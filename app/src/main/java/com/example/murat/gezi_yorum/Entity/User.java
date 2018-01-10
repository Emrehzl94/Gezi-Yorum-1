package com.example.murat.gezi_yorum.Entity;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Collects and manager user info
 */

public class User {
    public String token;
    public String username;
    public String name;
    public String surname;
    public String biography;
    public String website;
    public String email;
    public String phone;
    public String profilePicturePath;

    public static final String TOKEN = "access_token";
    public static final String PROFILEPHOTO = "profile_photo";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String BIOGRAPHY = "biography";
    public static final String WEBSITE = "website";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";

    public User(SharedPreferences preferences){
        token = preferences.getString(TOKEN,"");
        username = preferences.getString(USERNAME, "");
        name = preferences.getString(NAME,"");
        surname = preferences.getString(SURNAME,"");
        biography = preferences.getString(BIOGRAPHY,"");
        website = preferences.getString(WEBSITE,"");
        email = preferences.getString(EMAIL,"");
        phone = preferences.getString(PHONE,"");
        profilePicturePath = preferences.getString(PROFILEPHOTO, "");
    }
    public static void setArguments(String token, String username, JSONObject userInfo, String profilePicturePath, SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, token);
        editor.putString(USERNAME, username);
        editor.putString(PROFILEPHOTO, profilePicturePath);
        try {
            editor.putString(NAME, checkNull(userInfo.getString("name")));
            editor.putString(SURNAME, checkNull(userInfo.getString("surname")));
            editor.putString(BIOGRAPHY, checkNull(userInfo.getString("personalInfo")));
            editor.putString(WEBSITE, checkNull(userInfo.getString("website")));
            editor.putString(EMAIL, checkNull(userInfo.getString("email")));
            editor.putString(PHONE, checkNull(userInfo.getString("phone")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.apply();
    }
    private static String checkNull(String s){
        return s.equals("null") ? "" : s;
    }

    public void changeProfilePhoto(File newFile){
        File ppfile = new File(profilePicturePath);
        if(ppfile.exists()){
            ppfile.delete();
        }
        try {
            newFile.renameTo(new File(profilePicturePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void copyFile(InputStream source, File dest) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[5*1024*1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            source.close();
            os.close();
        }
    }
    public static Boolean validateEmail(String email){
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
