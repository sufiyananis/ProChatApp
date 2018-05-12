package com.example.sufiyananis.prochatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.sufiyananis.prochatapp.Adapter.ChatDialogAdapters;
import com.example.sufiyananis.prochatapp.Common.Common;
import com.example.sufiyananis.prochatapp.Holder.QBChatDialogHolder;
import com.example.sufiyananis.prochatapp.Holder.QBUnreadMessageHolder;
import com.example.sufiyananis.prochatapp.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogActivity extends AppCompatActivity implements QBSystemMessageListener,QBChatDialogMessageListener{

    FloatingActionButton floatingActionButton;
    ListView listChatDialogs;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        getMenuInflater().inflate(R.menu.chat_dialog_context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch(item.getItemId())
        {
            case R.id.context_delete_dialog:
                deleteDialog(info.position);
                break;
        }
        return true;
    }

    private void deleteDialog(int index) {
        final QBChatDialog chatDialog =(QBChatDialog)listChatDialogs.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(),false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                ChatDialogAdapters adapters = new ChatDialogAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                listChatDialogs.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.chat_dialog_menu_user:
                showUserProfile();
                break;
            default:
                break;
        }

        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(this,UserProfile.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialog);


        //Add Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("Pro Chat");
        setSupportActionBar(toolbar);

        listChatDialogs = findViewById(R.id.listChatDialogs);
        registerForContextMenu(listChatDialogs);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.chatdialog_adduser);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDialogActivity.this,ListUserActivity.class);
                startActivity(intent);
            }
        });

        listChatDialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                QBChatDialog qbChatDialog = (QBChatDialog)listChatDialogs.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogActivity.this,ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });


        createSessionForChat();

        loadChatDialogs();
    }

    private void loadChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                //put all dialog to cache
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                //unread setting
                Set<String> setIds = new HashSet<>();
                for(QBChatDialog chatDialog : qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //get message unread
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle()).performAsync(new QBEntityCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer, Bundle bundle) {
                        //save to cache
                        QBUnreadMessageHolder.getInstance().setBundle(bundle);

                        //Refresh List Dialogs
                        ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                        listChatDialogs.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
            Log.e("ERROR",""+e.getMessage());
            }
        });
    }

    private void createSessionForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialogActivity.this);
        mDialog.setMessage("Please wait....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String user,password;
        user=getIntent().getStringExtra("user");
        password=getIntent().getStringExtra("password");

        //load all user and save to cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser = new QBUser(user,password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }
                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {

                        mDialog.dismiss();
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",""+e.getMessage());
                        finish();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //put Dialog to cache
        //because we send system message with content is DialogId
        //so we can get dialog by Dialog Id
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //put to cache
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapters adapters = new ChatDialogAdapters(getBaseContext(),adapterSource);
                listChatDialogs.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR",""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
