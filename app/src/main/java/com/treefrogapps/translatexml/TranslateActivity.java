package com.treefrogapps.translatexml;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class TranslateActivity extends AppCompatActivity {

    private TranslateAdapter translateAdapter;
    private EditText englishWordsEditText;
    private ListView translateListView;
    private Button translateButton;

    private ArrayList<Translation> translationArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        englishWordsEditText = (EditText) findViewById(R.id.englishWordsEditText);
        translateButton = (Button) findViewById(R.id.translateButton);

        translateListView = (ListView) findViewById(R.id.translateListView);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkConnection() && !isEmpty(englishWordsEditText)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.get_trans), Toast.LENGTH_SHORT).show();
                    okHttpConnect(englishWordsEditText.getText().toString());

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_words), Toast.LENGTH_SHORT).show();
                }

            }
        });

    } // END OF onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean isEmpty(EditText editText) {

        return englishWordsEditText.getText().toString().trim().length() == 0;

    }   //END OF isEmpty

    public boolean checkConnection() {

        // system service connectivity manager
        // include in manifest : <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
        ConnectivityManager checkNetworkStatus = (ConnectivityManager)
                this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);

        // network info will get all the status
        NetworkInfo networkInfo = checkNetworkStatus.getActiveNetworkInfo();

        // check that the state is 'connected' (either wifi or phone network - only 1 connection type
        // can exist at the same time
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;

        } else {

            Toast.makeText(getApplication(), getResources().getString(R.string.no_net),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }   // END OF checkConnection


    public void okHttpConnect(String wordsToTranslate) {

        wordsToTranslate = wordsToTranslate.replace(" ", "+");
        String url = "http://www.treefrogapps.com/language/translateitxml.php?action=translations&english_words=" + wordsToTranslate;

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(9000, TimeUnit.MILLISECONDS);
        Request request = new Request.Builder()
                .addHeader("Content-Type", "text/xml")
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toast.makeText(getApplicationContext(), "Error while retrieving translations", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (response.code() == 200) {

                    InputStream inputStream = response.body().byteStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {

                        stringBuilder.append(line);
                    }

                    String xmlString = stringBuilder.toString();
                    Log.v("XML STRING : ", xmlString);


                    try {

                        outputTranslations(xmlString);

                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Error : " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void outputTranslations(String xmlString) throws XmlPullParserException, IOException {

        // create a new instance of XmlPullParserFactory
        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
        pullParserFactory.setNamespaceAware(true);

        // create a new XmlPullParser to pull the data from the string
        XmlPullParser xmlPullParser = pullParserFactory.newPullParser();
        xmlPullParser.setInput(new StringReader(xmlString));

        // Xml has EventType for START and END of document
        // Also <TAG></TAG> are event types - the DATA is the text between the tags
        int eventType = xmlPullParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT){

            String language = null;
            String itemTranslation = null;
            Translation translation = new Translation();

            // if statement to see what the next tags are (we want a start tag)
            // but want to skip empty tags to the ones that contain data/text

            switch (eventType){

                case XmlPullParser.END_TAG :


                    if(xmlPullParser.getName().equals("language")){

                        language = xmlPullParser.getText();
                        translation.setLanguage(language);

                    }  else if (xmlPullParser.getName().equals("translation")){

                        itemTranslation = xmlPullParser.nextText();
                        translation.setTranslation(itemTranslation);
                        translationArrayList.add(translation);
                    }

                    xmlPullParser.next();
                    break;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                translateAdapter = new TranslateAdapter(translationArrayList, getApplicationContext());
                translateListView.setAdapter(translateAdapter);
            }
        });

    } // END OF outputTranslations


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle persistableBundle) {
        super.onSaveInstanceState(outState, persistableBundle);
    }
}
