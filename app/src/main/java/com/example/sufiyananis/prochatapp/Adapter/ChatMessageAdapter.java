package com.example.sufiyananis.prochatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sufiyananis.prochatapp.Holder.QBUsersHolder;
import com.example.sufiyananis.prochatapp.R;
import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sufiyan Anis on 4/8/2018.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
       if(convertView==null)
       {
           LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          if(qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId()))
           {
               view = inflater.inflate(R.layout.list_send_message,null);
               BubbleTextView bubbleTextView = (BubbleTextView)view.findViewById(R.id.message_content);
               bubbleTextView.setText(qbChatMessages.get(position).getBody());
           }
           else  if(!qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId()))
           {
               view = inflater.inflate(R.layout.list_recv_message,null);
               BubbleTextView bubbleTextView = (BubbleTextView)view.findViewById(R.id.message_content);
               bubbleTextView.setText(QBUsersHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getLogin()+"\n\n"+qbChatMessages.get(position).getBody());
               Calendar calendar = Calendar.getInstance();
               SimpleDateFormat format = new SimpleDateFormat("HH:MM:SS");
               TextView txttime = (TextView)view.findViewById(R.id.message_time);
               txttime.setText(format.format(calendar.getTime()));

           }
       }

        return view;
    }
}
