package com.example.murat.gezi_yorum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Utils.MultipartUtility;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EditProfile extends AppCompatActivity {
    private EditText name_edit, surname_edit, biography_edit, website_edit, email_edit, phone_edit;
    private SharedPreferences preferences;
    private ImageView profilePhoto;
    private User user;
    private Handler handler;
    private static final int GALLERY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePhoto = findViewById(R.id.profilePhoto);
        name_edit = findViewById(R.id.name_edit);
        surname_edit = findViewById(R.id.surname_edit);
        biography_edit= findViewById(R.id.biography_edit);
        website_edit = findViewById(R.id.website_edit);
        email_edit = findViewById(R.id.email_edit);
        phone_edit = findViewById(R.id.phone_edit);

        preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        user = new User(preferences);
        name_edit.setText(user.name);
        surname_edit.setText(user.surname);
        biography_edit.setText(user.biography);
        website_edit.setText(user.website);
        email_edit.setText(user.email);
        phone_edit.setText(user.phone);

        profilePhoto.setImageBitmap(ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(user.profilePicturePath), 400, 400
        ));

        Button changepp = findViewById(R.id.change_pp);
        changepp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select)), GALLERY);
            }
        });
        Button saveGeneralInfo = findViewById(R.id.save);
        saveGeneralInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                //noinspection ConstantConditions
                if(cm.getActiveNetworkInfo() == null) {
                    Toast.makeText(EditProfile.this,
                            getString(R.string.internet_warning),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String name = name_edit.getText().toString();
                String surname = surname_edit.getText().toString();
                String email = email_edit.getText().toString();
                if(name.equals("")){
                    name_edit.requestFocus();
                    name_edit.setError(getString(R.string.name) + " " + getString(R.string.cannot_empty));
                    return;
                }else if(surname.equals("")){
                    surname_edit.requestFocus();
                    surname_edit.setError(getString(R.string.surname) + " " + getString(R.string.cannot_empty));
                    return;
                }else if(!User.validateEmail(email)){
                    email_edit.requestFocus();
                    email_edit.setError(getString(R.string.error_invalid_email));
                    return;
                }
                JSONObject userInfo = new JSONObject();
                try {
                    userInfo.put("name", name);
                    userInfo.put("surname", surname);
                    userInfo.put("personalInfo", biography_edit.getText().toString());
                    userInfo.put("email", email);
                    userInfo.put("website", website_edit.getText().toString());
                    userInfo.put("phone", phone_edit.getText().toString());
                    new Thread(new GeneralInfoUpdater(userInfo, user.token)).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        handler = new Handler();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            //noinspection ConstantConditions
            if(cm.getActiveNetworkInfo() == null) {
                Toast.makeText(this,
                        getString(R.string.internet_warning),
                        Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Toast.makeText(this, R.string.uploading_picture, Toast.LENGTH_LONG).show();
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                user.changeProfilePhoto(inputStream);
                profilePhoto.setImageBitmap(ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(user.profilePicturePath), 400, 400
                ));
                new Thread(new PPUploader(user.profilePicturePath, user.token)).start();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this,
                        getString(R.string.error),
                        Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this,
                    getString(R.string.no_file_selected),
                    Toast.LENGTH_LONG).show();
        }
    }

    private class PPUploader implements Runnable {
        private File photo;
        private String token;

        PPUploader(String path, String token){
            photo = new File(path);
            this.token = token;
        }
        @Override
        public void run() {
            MultipartUtility multipart;
            String URL = Constants.APP+"uploadProfilePhoto";
            try {
                multipart = new MultipartUtility(URL,"UTF-8", false);
                multipart.addFormField("token", token);
                multipart.addFilePart("file",photo);
                List<String> messages = multipart.finish();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.pp_success), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (IOException ex) {
               ex.printStackTrace();
               handler.post(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error), Toast.LENGTH_LONG).show();
                   }
               });
            }
        }
    }

    private class GeneralInfoUpdater implements Runnable{
        private JSONObject userInfo;
        private String token;

        GeneralInfoUpdater(JSONObject userInfo, String token){
            this.userInfo = userInfo;
            this.token = token;
        }

        @Override
        public void run() {
            JSONObject updateRequest = new JSONObject();
            try {
                updateRequest.put("token", this.token);
                updateRequest.put("user", userInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = Constants.APP + "saveUserGeneralInfo";
            URLRequestHandler requestHandler = new URLRequestHandler(updateRequest.toString(), url);
            if(requestHandler.getResponseMessage()){
                User.setArguments(user.token, user.username, userInfo, user.profilePicturePath, preferences);
                user = new User(preferences);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.update_successful, Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                }
            });

        }
    }
}
