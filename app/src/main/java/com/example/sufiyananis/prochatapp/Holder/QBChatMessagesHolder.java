package com.example.sufiyananis.prochatapp.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sufiyan Anis on 4/8/2018.
 */

public class QBChatMessagesHolder {
    private static QBChatMessagesHolder instance;
    private HashMap<String,ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized  QBChatMessagesHolder getIntance(){
        QBChatMessagesHolder qbChatMessagesHolder;
        synchronized (QBChatMessagesHolder.class)
        {
            if(instance == null)
                instance = new QBChatMessagesHolder();
            qbChatMessagesHolder = instance;
        }
        return qbChatMessagesHolder;
    }

    private QBChatMessagesHolder()
    {
        this.qbChatMessageArray = new HashMap<>();
    }

    public void putMessages(String dialogid,ArrayList<QBChatMessage> qbChatMessages)
    {
        this.qbChatMessageArray.put(dialogid,qbChatMessages);
    }

    public void putMessage(String dialogid,QBChatMessage qbChatMessage)
    {
        List<QBChatMessage> listResult =(List)this.qbChatMessageArray.get(dialogid);
        listResult.add(qbChatMessage);
        ArrayList<QBChatMessage> listAdded = new ArrayList<>(listResult.size());
        listAdded.addAll(listResult);
        putMessages(dialogid,listAdded);
    }

    public ArrayList<QBChatMessage>  getChatMessagesByDialogId(String dialogid)
    {
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogid);
    }
}
