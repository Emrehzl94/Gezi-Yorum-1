package com.example.murat.gezi_yorum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final int MAIN_ACTIVITY_RESULT = 1;

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView emailEdit;
    private EditText passwordEdit;
    private View progressView;
    private View loginFormView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent2 = new Intent(this, ZipFileDownloader.class);
        intent2.putExtra("url", Environment.getExternalStoragePublicDirectory(getApplicationContext().getString(R.string.app_name)) + "/trip_1.zip");
        startService(intent2);

        CookieManager manager = CookieManager.getInstance();
        // Set up the login form.
        String token = manager.getCookie(Constants.ROOT);
        if(token != null){
            Intent intent = new Intent(this,MainActivity.class);
            startActivityForResult(intent, MAIN_ACTIVITY_RESULT);
        }

        Button without_login = findViewById(R.id.without_login);
        without_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        emailEdit = findViewById(R.id.email);

        passwordEdit = findViewById(R.id.password);

        Button signInButton = findViewById(R.id.sign_in);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        Button registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class );
                startActivity(intent);
            }
        });

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        emailEdit.setError(null);
        passwordEdit.setError(null);

        // Store values at the time of the login attempt.
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError(getString(R.string.error_invalid_password));
            focusView = passwordEdit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_CANCELED){
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {

                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
     class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String uname;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            uname = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String data = "{\"username\":\"" + uname + "\",\"password\":\"" + mPassword + "\"}";
            String url = "http://163.172.176.169:8080/Geziyorum/login";
            URLRequestHandler handler = new URLRequestHandler(data,url);
            if(!handler.getResponseMessage()){
                //wrong user name or password
                return false;
            }

            String response = handler.getResponse();

            CookieManager manager = CookieManager.getInstance();
            manager.setCookie(Constants.ROOT,   Constants.TOKEN+"="+response);
            manager.setCookie(Constants.ROOT,   Constants.APPLICATION+"=true");

            SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE).edit();

            editor.putString(Constants.USERNAME, uname);
            editor.putString(Constants.TOKEN, response);

            handler = new URLRequestHandler(uname, Constants.APP+"downloadProfilePhotoPath");
            handler.getResponseMessage();
            String link = handler.getResponse();

            try {
                String profilePicturePath = getFilesDir() + "/profile.jpg";
                URL website = new URL(Constants.ROOT+link);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(profilePicturePath);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                editor.putString(Constants.PROFILEPHOTO, profilePicturePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.apply();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            } else {
                passwordEdit.setError(getString(R.string.error_incorrect_password));
                passwordEdit.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

