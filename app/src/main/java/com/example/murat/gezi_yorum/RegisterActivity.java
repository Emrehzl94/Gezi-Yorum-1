package com.example.murat.gezi_yorum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ProgressBar registerProgress;
    private LinearLayout controlFrame;
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
        registerProgress = findViewById(R.id.register_progress);
        controlFrame = findViewById(R.id.controlFrame);

        ImageButton look_pass = findViewById(R.id.look_pass);
        look_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pass1_edit.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)){
                    pass1_edit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else {
                    pass1_edit.setInputType((InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                }
            }
        });
        ImageButton look_pass_again = findViewById(R.id.look_pass_again);
        look_pass_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pass2_edit.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) ){
                    pass2_edit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else {
                    pass2_edit.setInputType((InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                }
            }
        });
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
        private Handler handler;

        UserRegister(String uname, String password, String email, String name, String surname) {
            this.uname = uname;
            this.password = password;
            this.email = email;
            this.name = name;
            this.surname = surname;
            registerProgress.setVisibility(View.VISIBLE);
            controlFrame.setVisibility(View.GONE);
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
            return handler.getResponseMessage();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                Toast.makeText(getApplicationContext(), getString(R.string.register_ok), Toast.LENGTH_LONG).show();
                finish();
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
            }
            registerProgress.setVisibility(View.GONE);
            controlFrame.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {

        }
    }
}
