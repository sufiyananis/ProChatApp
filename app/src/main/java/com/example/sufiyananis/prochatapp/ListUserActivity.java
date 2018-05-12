package com.example.sufiyananis.prochatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sufiyananis.prochatapp.Adapter.ListUserAdapter;
import com.example.sufiyananis.prochatapp.Common.Common;
import com.example.sufiyananis.prochatapp.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUserActivity extends AppCompatActivity {

    ListView listUsers;
    Button btnCreateChat;

    String mode="";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        mode=getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog=(QBChatDialog)getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);

        btnCreateChat=findViewById(R.id.btn_create_chat);
        listUsers =findViewById(R.id.listUsers);
        listUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == null) {
                    int countchoise = listUsers.getCount();

                    if (listUsers.getCheckedItemPositions().size() == 1) {
                        createprivateChat(listUsers.getCheckedItemPositions());
                    } else if (listUsers.getCheckedItemPositions().size() > 1)
                        createGroupChat(listUsers.getCheckedItemPositions());
                    else
                        Toast.makeText(ListUserActivity.this, "Please select friend to chat", Toast.LENGTH_SHORT).show();
                }else if(mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog!=null)
                {
                    if(userAdd.size() > 0)
                    {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoise = listUsers.getCount();
                        SparseBooleanArray checkItemPositions = listUsers.getCheckedItemPositions();
                        for(int i=0;i<cntChoise;i++)
                        {
                            if(checkItemPositions.get(i))
                            {
                                QBUser user =(QBUser)listUsers.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }
                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(getBaseContext(),"user added successfully",Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

                    }
                }else if(mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog!=null)
                {
                    if(userAdd.size() > 0)
                    {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoise = listUsers.getCount();
                        SparseBooleanArray checkItemPositions = listUsers.getCheckedItemPositions();
                        for(int i=0;i<cntChoise;i++)
                        {
                            if(checkItemPositions.get(i))
                            {
                                QBUser user =(QBUser)listUsers.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }
                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(getBaseContext(),"user removed successfully",Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

                    }
                }
            }
        });

if(mode==null && qbChatDialog==null)
    retrieveAllUser();
else{
    if(mode.equals(Common.UPDATE_ADD_MODE))
        loadListAvailableUser();
    else if(mode.equals(Common.UPDATE_REMOVE_MODE))
        loadListUserInGroup();
}

    }

    private void loadListUserInGroup() {
        btnCreateChat.setText("Remove User");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUserAlreadyInGroup= QBUsersHolder.getInstance().getUsersByIds(occupantsId);
                ArrayList<QBUser> users = new ArrayList<>();
                users.addAll(listUserAlreadyInGroup);

                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),users);
                listUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                userAdd=users;
            }

            @Override
            public void onError(QBResponseException e) {
            Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadListAvailableUser() {
        btnCreateChat.setText("Add User");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ArrayList<QBUser> lstUsers =QBUsersHolder.getInstance().getAllUsers();
                List<Integer> occupantsId = qbChatDialog.getOccupants();
                List<QBUser> listUserAlreadyInChatGroup =QBUsersHolder.getInstance().getUsersByIds(occupantsId);


                //remove all users that already in group
                for(QBUser user:listUserAlreadyInChatGroup)
                    lstUsers.remove(user);
                if(lstUsers.size() > 0)
                {
                        ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),lstUsers);
                        listUsers.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        userAdd=lstUsers;

                }

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


    private void createprivateChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialog = new ProgressDialog(ListUserActivity.this);
        mDialog.setMessage("Please wait....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countchoice = listUsers.getCount();

        for(int i=0;i<countchoice;i++){
            if(checkedItemPositions.get(i)){
                final QBUser user =(QBUser)listUsers.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(ListUserActivity.this, "Create private Chat Dialog successfully", Toast.LENGTH_SHORT).show();
                        //send system message to recipient id user
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }


                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",""+e.getMessage());
                    }
                });
            }
        }

    }
    private void createGroupChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog mDialog = new ProgressDialog(ListUserActivity.this);
        mDialog.setMessage("Please wait....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countchoice = listUsers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for(int i=0;i<countchoice;i++){
            if(checkedItemPositions.get(i)){
                QBUser user =(QBUser)listUsers.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }

        //create chat dialog
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialog.dismiss();
                Toast.makeText(ListUserActivity.this, "Create Chat Dialog successfully", Toast.LENGTH_SHORT).show();
                //send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for(int i=0;i<qbChatDialog.getOccupants().size();i++)
                {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                }

                finish();
            }

            @Override
            public void onError(QBResponseException e) {
            Log.e("ERROR",""+e.getMessage());
            }
        });
    }
    private void retrieveAllUser() {

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                //Add to cache
                QBUsersHolder.getInstance().putUsers(qbUsers);
                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                for(QBUser user : qbUsers){
                    if(!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                    qbUserWithoutCurrent.add(user);

                }

                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),qbUserWithoutCurrent);
                listUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
            Log.e("ERROR",""+e.getMessage());
            }
        });
    }
}
