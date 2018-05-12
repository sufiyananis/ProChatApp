package com.example.sufiyananis.prochatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID ="69964";
    static final String AUTH_KEY ="F2zp7bJF9GhXyQu";
    static final String AUTH_SECRET ="AzCVgYdsmF3z2JS";
    static final String ACCOUNT_KEY ="zjKUfKTGdXzJJjs79sa1";
    static final int REQUEST_CODE = 1000;

    Button btnLogin,btnSignup;
    EditText edtUser,edtPassword;
    RelativeLayout rellay1,rellay2;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
            rellay2.setVisibility(View.VISIBLE);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rellay1= findViewById(R.id.rellay1);
        rellay2=findViewById(R.id.rellay2);

        handler.postDelayed(runnable,2000);

        initializeFramework();

        btnLogin = (Button) findViewById(R.id.main_btnLogin);
        btnSignup = (Button)findViewById(R.id.main_btnSignup);
        edtUser =(EditText) findViewById(R.id.main_editLogin);
        edtPassword=(EditText)findViewById(R.id.main_editpassword);


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SignupActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user = edtUser.getText().toString();
                final String password = edtPassword.getText().toString();


                QBUser qbUser = new QBUser(user,password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(MainActivity.this, "Login Succesfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this,ChatDialogActivity.class);
                        intent.putExtra("user",user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();
                        edtUser.setText("");
                        edtPassword.setText("");

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initializeFramework() {
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
