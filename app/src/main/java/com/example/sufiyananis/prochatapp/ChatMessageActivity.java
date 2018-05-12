package com.example.sufiyananis.prochatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.sufiyananis.prochatapp.Adapter.ChatMessageAdapter;
import com.example.sufiyananis.prochatapp.Common.Common;
import com.example.sufiyananis.prochatapp.Holder.QBChatMessagesHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView listChatMessages;
    ImageButton submitButton;
    EditText edtContent;
    ChatMessageAdapter adapter;
    Toolbar toolbar;


    //update online User
    TextView  txt_online_count;
    ImageView img_online_count,dialog_avatar;
    //variable for Edit/Delete message
    int contextMenuIndexClicked =-1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //get idex context menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch(item.getItemId())
        {
            case R.id.chat_message_update_message:
                updateMessage();
                break;
            case R.id.chat_message_delete_message:
                deleteMessage();
                break;
                default:
                    break;
        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog deleteDialog = new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setMessage("Please wait...");
        deleteDialog.show();
        editMessage = QBChatMessagesHolder.getIntance().getChatMessagesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);

        QBRestChatService.deleteMessage(editMessage.getId(),false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessage();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    private void updateMessage() {
        //set message for edit text
        editMessage = QBChatMessagesHolder.getIntance().getChatMessagesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);
        edtContent.setText(editMessage.getBody());
        isEditMode = true;
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if(qbChatDialog.getType()==QBDialogType.GROUP || qbChatDialog.getType()== QBDialogType.PUBLIC_GROUP )
//        getMenuInflater().inflate(R.menu.chat_message_group_menu,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId())
//        {
//            case R.id.chat_group_edit_name:
//                editNameGroup();
//                break;
//            case R.id.chat_group_add_user:
//                addUser();
//                break;
//            case R.id.chat_group_remove_user:
//                removeUser();
//                break;
//        }
//        return true;
//    }

    private void removeUser() {
        Intent intent = new Intent(this,ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(this,ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editNameGroup() {
        //alert dialogue that user edit name of group
        LayoutInflater inflater =LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName =(EditText)view.findViewById(R.id.edt_new_group_name);

        //set Dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                qbChatDialog.setName(newName.getText().toString()); //set new Name of group

                QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Toast.makeText(ChatMessageActivity.this, "Group name changed successfully", Toast.LENGTH_SHORT).show();
                        toolbar.setTitle(qbChatDialog.getName());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        //create alertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu,menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initViews();

        initChatDialogs();
        retrieveMessage();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edtContent.getText().toString().isEmpty()) {
                    if (!isEditMode) {
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setBody(edtContent.getText().toString());
                        chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                        chatMessage.setSaveToHistory(true);

                        try {
                            qbChatDialog.sendMessage(chatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        //fix the problem the group chat message also show in private so:
                        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                            QBChatMessagesHolder.getIntance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                            ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getIntance().getChatMessagesByDialogId(chatMessage.getDialogId());
                            adapter = new ChatMessageAdapter(getBaseContext(), messages);
                            listChatMessages.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
//                // put message into cache
//                QBChatMessagesHolder.getIntance().putMessage(qbChatDialog.getDialogId(),chatMessage);
//                ArrayList<QBChatMessage> messages =QBChatMessagesHolder.getIntance().getChatMessagesByDialogId(qbChatDialog.getDialogId());
//                adapter = new ChatMessageAdapter(getBaseContext(),messages);
//                listChatMessages.setAdapter(adapter);
//                adapter.notifyDataSetChanged();

                        //remove text from editcontent edittext
                        edtContent.setText("");
                        edtContent.setFocusable(true);
                    } else {

                        final ProgressDialog updateDialog = new ProgressDialog(ChatMessageActivity.this);
                        updateDialog.setMessage("Please wait...");
                        updateDialog.show();
                        QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                        messageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();
                        QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder).performAsync(new QBEntityCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                //refresh data
                                retrieveMessage();
                                isEditMode = false;
                                updateDialog.dismiss();

                                //reset edit text
                                edtContent.setText("");
                                edtContent.setFocusable(true);

                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

    }




    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500); //get limit of 500 messages

        if(qbChatDialog !=null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //put message to cache
                    QBChatMessagesHolder.getIntance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(),qbChatMessages);
                    listChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }

    }

    private void initChatDialogs() {
        qbChatDialog =(QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);

        qbChatDialog.initForChat(QBChatService.getInstance());
        //regiter listener Incoming message
        QBIncomingMessagesManager incomingMessages = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessages.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //join group to enable group chat
        if(qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d("ERROR",""+e.getMessage());
                }
            });

        }

        QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if(dialogId == qbChatDialog.getDialogId())
                {
                    QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                            try {
                                Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig().withBorder(4).endConfig().round();
                                TextDrawable online =builder.build("", Color.RED);
                                txt_online_count.setText(String.format("%d/%d online"+onlineList.size(),qbChatDialog.getOccupants().size()));
                                img_online_count.setImageDrawable(online);
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });


                }
            }
        };
        qbChatDialog.addParticipantListener(participantListener);

        qbChatDialog.addMessageListener(this);

        //set title of toolbar
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);
    }

    private void initViews() {
        listChatMessages =(ListView)findViewById(R.id.list_of_message);
        submitButton=(ImageButton)findViewById(R.id.send_Button);
        edtContent=(EditText)findViewById(R.id.edt_content);

        img_online_count = (ImageView)findViewById(R.id.img_online_count);
        txt_online_count=(TextView)findViewById(R.id.txt_online_count);


        //Add context menu
        registerForContextMenu(listChatMessages);

        //Add toolbar
         toolbar = (Toolbar)findViewById(R.id.chat_message_toolbar);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //cache message
        QBChatMessagesHolder.getIntance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getIntance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(),messages);
        listChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
    Log.e("ERROR",""+e.getMessage());
    }
}
