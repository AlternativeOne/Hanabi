package com.lexoff.animediary;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomSpinnerArrayAdapter extends ArrayAdapter<CharSequence> {

    public CustomSpinnerArrayAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View itemView =  super.getDropDownView(position, convertView, parent);

        itemView.setBackgroundColor(Color.parseColor("#171717"));

        return itemView;
    }
}