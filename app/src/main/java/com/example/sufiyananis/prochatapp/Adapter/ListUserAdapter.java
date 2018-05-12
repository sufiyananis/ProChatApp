package com.example.sufiyananis.prochatapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sufiyananis.prochatapp.ListUserActivity;
import com.example.sufiyananis.prochatapp.R;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by Sufiyan Anis on 4/8/2018.
 */

public class ListUserAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBUser> qbUserArrayList;

    public ListUserAdapter(Context context, ArrayList<QBUser> qbUserArrayList) {
        this.context = context;
        this.qbUserArrayList = qbUserArrayList;
    }

    @Override
    public int getCount() {
        return qbUserArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return qbUserArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_multiple_choice,null);
            TextView textView =(TextView)view.findViewById(android.R.id.text1);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setText(qbUserArrayList.get(position).getLogin());
        }
        return view;
    }
}
