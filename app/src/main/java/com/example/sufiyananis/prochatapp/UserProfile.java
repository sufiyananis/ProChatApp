package com.example.sufiyananis.prochatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.sufiyananis.prochatapp.Common.Common;
import com.example.sufiyananis.prochatapp.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserProfile extends AppCompatActivity {

    EditText edtPassword,edtoldPassword,edtFullName,edtEmail,edtPhone;
    Button btnUpdate,btnCancel;
    ImageView user_avatar;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.user_update_log_out:
                logOut();
                break;
                default:
                    break;
        }
        return true;
    }

    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfile.this, "Logged Out!!!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfile.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //remove previous acctivity
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.user_update_toolbar);
        toolbar.setTitle("Pro Chat");
        setSupportActionBar(toolbar);

        initViews();
        loadUserProfile();


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String password = edtPassword.getText().toString();
                String oldPassword =edtoldPassword.getText().toString();
                String email = edtEmail.getText().toString();
                String fullname = edtFullName.getText().toString();
                String Phone = edtPhone.getText().toString();

                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                if(!Common.isNullorEmptyString(oldPassword))
                    user.setOldPassword(oldPassword);
                if(!Common.isNullorEmptyString(password))
                    user.setPassword(password);
                if(!Common.isNullorEmptyString(fullname))
                    user.setFullName(fullname);
                if(!Common.isNullorEmptyString(email))
                    user.setEmail(email);
                if(!Common.isNullorEmptyString(Phone))
                    user.setPhone(Phone);
                final ProgressDialog mDialog = new ProgressDialog(UserProfile.this);
                mDialog.setMessage("Please wait...");
                mDialog.show();
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(UserProfile.this, "User: "+qbUser.getLogin()+" updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfile.this, "ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadUserProfile() {
        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullname = currentUser.getFullName();
        String email =currentUser.getEmail();
        String phone = currentUser.getPhone();

        edtEmail.setText(email);
        edtFullName.setText(fullname);
        edtPhone.setText(phone);
    }

    private void initViews() {
        btnCancel = findViewById(R.id.update_user_btn_cancel);
        btnUpdate =findViewById(R.id.update_user_btn_update);

        edtEmail=findViewById(R.id.update_edt_email);
        edtFullName=findViewById(R.id.update_edt_full_name);
        edtPhone=findViewById(R.id.update_edt_phone);
        edtPassword=findViewById(R.id.update_edt_password);
        edtoldPassword=findViewById(R.id.update_edt_old_password);


    }
}
