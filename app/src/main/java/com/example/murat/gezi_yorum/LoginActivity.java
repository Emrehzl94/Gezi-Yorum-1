package com.example.murat.gezi_yorum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = new NotificationChannel(Constants.CH1, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableLights(true);
            manager.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(Constants.CH2, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setSound(null, null);
            manager.createNotificationChannel(mChannel);

        }

        SharedPreferences preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        Constants.ROOT = preferences.getString("root", Constants.ROOT);
        Constants.APP = Constants.ROOT + "/Geziyorum/";

        CookieManager manager = CookieManager.getInstance();
        // Set up the login form.
        String token = manager.getCookie(Constants.ROOT);
        if(token != null){
            Intent intent = new Intent(this,MainActivity.class);
            startActivityForResult(intent, MAIN_ACTIVITY_RESULT);
        }

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
        Button forgetPassword = findViewById(R.id.forgetten_password);
        forgetPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(getString(R.string.forget_password));
                builder.setMessage(R.string.username);
                final EditText username_edit = new EditText(LoginActivity.this);
                username_edit.setId(0);
                builder.setView(username_edit);
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(LoginActivity.this, R.string.please_wait, Toast.LENGTH_LONG).show();
                        String username = username_edit.getText().toString();
                        new Thread(new SendForgetPassword(username)).start();
                    }
                });
                builder.create();
                builder.show();
            }
        });

    }

    private class SendForgetPassword implements Runnable{
        private String username;

        SendForgetPassword(String username){
            this.username = username;
        }
        @Override
        public void run() {
            URLRequestHandler requestHandler = new URLRequestHandler(username, Constants.APP + "forgotPassword");
            if(requestHandler.getResponseMessage()){
                if(requestHandler.getResponse().equals("true")){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(R.string.forget_password_ok);
                            builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.create();
                            builder.show();
                        }
                    });
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
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
        private int errorMessageId;

        UserLoginTask(String email, String password) {
            uname = email;
            mPassword = password;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = Constants.APP+"checkUserActivated";
            URLRequestHandler requestHandler = new URLRequestHandler(uname, url);
            if(requestHandler.getResponseMessage()){
                String response = requestHandler.getResponse();
                if(response.equals("false")){
                    errorMessageId = R.string.email_activation_required;
                    return false;
                }else if(response.equals("notExist")){
                    errorMessageId = R.string.user_not_exist;
                    return false;
                }
            }

            String data = "{\"username\":\"" + uname + "\",\"password\":\"" + mPassword + "\"}";
            url = Constants.APP+"login";
            requestHandler = new URLRequestHandler(data,url);
            if(!requestHandler.getResponseMessage()){
                //wrong user name or password
                errorMessageId = R.string.error_incorrect_password;
                return false;
            }

            String token = requestHandler.getResponse();
            if(token == null || token.equals("")){
                errorMessageId = R.string.error;
                return false;
            }

            requestHandler = new URLRequestHandler(token, Constants.APP + "getUserInfoByToken");
            if(!requestHandler.getResponseMessage()){
                errorMessageId = R.string.error;
                return false;
            }
            JSONObject userInfo;
            try {
                userInfo = new JSONObject(requestHandler.getResponse());
            } catch (JSONException e) {
                errorMessageId = R.string.error;
                return false;
            }

            CookieManager manager = CookieManager.getInstance();
            manager.setCookie(Constants.ROOT,   User.TOKEN+"="+token);
            manager.setCookie(Constants.ROOT,   Constants.APPLICATION+"=true");
            SharedPreferences preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);

            requestHandler = new URLRequestHandler(uname, Constants.APP+"downloadProfilePhotoPath");
            requestHandler.getResponseMessage();
            String link = requestHandler.getResponse();
            String profilePicturePath = getFilesDir() + "/profile.jpg";
            try {
                URL website = new URL(Constants.ROOT+link);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(profilePicturePath);
                fos.getChannel().transferFrom(rbc, 0, 5242880);
            } catch (IOException e) {
                errorMessageId = R.string.error;
                return false;
            }
            User.setArguments(token, uname, userInfo, profilePicturePath, preferences);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivityForResult(intent, MAIN_ACTIVITY_RESULT);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(getString(errorMessageId));
                builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

