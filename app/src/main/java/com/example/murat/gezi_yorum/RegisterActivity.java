package com.example.murat.gezi_yorum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class RegisterActivity extends AppCompatActivity {
    private EditText username_edit, pass1_edit, pass2_edit, email_edit, name_edit, surname_edit;
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
        name_edit = findViewById(R.id.name_edit);
        surname_edit = findViewById(R.id.surname_edit);
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
            String name = name_edit.getText().toString();
            String surname = surname_edit.getText().toString();
            String email = email_edit.getText().toString();
            CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
            if(!asciiEncoder.canEncode(username)){
                username_edit.requestFocus();
                username_edit.setError(getString(R.string.username_wrong));
                return;
            }else if(username.equals("")){
                username_edit.requestFocus();
                username_edit.setError(getString(R.string.username_empty));
                return;
            }else if(name.equals("")){
                name_edit.requestFocus();
                name_edit.setError(getString(R.string.name) + " " + getString(R.string.cannot_empty));
                return;
            }else if(surname.equals("")){
                surname_edit.requestFocus();
                surname_edit.setError(getString(R.string.surname) + " " + getString(R.string.cannot_empty));
                return;
            }else if(!User.validateEmail(email)) {
                email_edit.requestFocus();
                email_edit.setError(getString(R.string.error_invalid_email));
                return;
            }

            new UserRegister(username, pass1, email, name, surname).execute();

            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserRegister extends AsyncTask<Void, Void, Boolean> {

        private String uname;
        private String password;
        private String email;
        private String name;
        private String surname;

        UserRegister(String uname, String password, String email, String name, String surname) {
            this.uname = uname;
            this.password = password;
            this.email = email;
            this.name = name;
            this.surname = surname;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject user_info = new JSONObject();
            try {
                user_info.put("username", uname);
                user_info.put("password",password);
                user_info.put("email",email);
                user_info.put("name",name);
                user_info.put("surname",surname);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Constants.APP+"userRegister";
            URLRequestHandler handler = new URLRequestHandler(user_info.toString(), url);
            if(!handler.getResponseMessage()){
                //unsuccesful
                return false;
            }

            String token = handler.getResponse();

            CookieManager manager = CookieManager.getInstance();
            manager.setCookie(Constants.ROOT,   User.TOKEN+"="+token);
            manager.setCookie(Constants.ROOT,   Constants.APPLICATION+"=true");
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                Toast.makeText(getApplicationContext(), getString(R.string.register_ok), Toast.LENGTH_LONG).show();
                finish();
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
