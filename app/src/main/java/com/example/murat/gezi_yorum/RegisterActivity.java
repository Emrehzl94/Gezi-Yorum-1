package com.example.murat.gezi_yorum;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

public class RegisterActivity extends AppCompatActivity {
    private EditText username_edit, pass1_edit, pass2_edit, email_edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        username_edit = findViewById(R.id.username_edit);
        pass1_edit = findViewById(R.id.password_edit);
        pass2_edit = findViewById(R.id.password2_edit);
        email_edit = findViewById(R.id.email_edit);
        FloatingActionButton registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String pass1 = pass1_edit.getText().toString();
            String pass2 = pass2_edit.getText().toString();
            if(!pass1.equals(pass2)){
                Snackbar.make(view,getString(R.string.passwords_not_same),Snackbar.LENGTH_LONG).show();
                return;
            }
            String username = username_edit.getText().toString();
            String email = email_edit.getText().toString();
            String data = "{\"username\":\""+username+"\",\"password\":\""+pass1+"\",\"email\":\""+email+"\"}";
            String url = "http://163.172.176.169:8080/Geziyorum/userRegister";
            URLRequestHandler handler = new URLRequestHandler(data,url);
            if(!handler.getResponseMessage()){
                //kayıt başarısız
                return;
            }
            Toast.makeText(getApplicationContext(),  getString(R.string.register_ok),Toast.LENGTH_SHORT).show();

            finish();

            }
        });
    }

}
