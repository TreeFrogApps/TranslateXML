package com.treefrogapps.translatexml;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TranslateAdapter extends BaseAdapter {

    private ArrayList<Translation> translationArrayList;
    private Context context;
    private LayoutInflater layoutInflater;

    private TextView languageTextView;
    private TextView translationTextView;



    public TranslateAdapter(ArrayList<Translation> translationArrayList, Context context){
        super();

        this.translationArrayList = translationArrayList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);




    }

    private static class ViewHolder{

        protected TextView languageTextView;
        protected TextView translationTextView;
    }


    @Override
    public int getCount() {
        return translationArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;


        if (convertView == null) {

            viewHolder = new ViewHolder();

            convertView = layoutInflater.inflate(R.layout.listview_translate_item, null);

            viewHolder.languageTextView = (TextView) convertView.findViewById(R.id.textViewLanguage);
            viewHolder.translationTextView = (TextView) convertView.findViewById(R.id.textViewTranslation);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.languageTextView.setText(translationArrayList.get(position).getLanguage().toUpperCase());
        viewHolder.translationTextView.setText(translationArrayList.get(position).getTranslation());

        return convertView;
    }
}
