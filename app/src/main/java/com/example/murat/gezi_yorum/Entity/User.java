package com.example.murat.gezi_yorum.Entity;

import android.content.SharedPreferences;

/**
 * Collects and manager user info
 */

public class User {
    public String token;
    public String username;
    public String name_surname;
    public String profilePicturePath;

    public static final String TOKEN = "access_token";
    public static final String PROFILEPHOTO = "profile_photo";
    public static final String USERNAME = "username";
    public static final String NAME_SURNAME = "name_surname";

    public User(SharedPreferences preferences){
        token = preferences.getString(TOKEN,"");
        username = preferences.getString(USERNAME, "");
        profilePicturePath = preferences.getString(PROFILEPHOTO, "");
    }
    public static void setArguments(String token, String username, String profilePicturePath, SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, token);
        editor.putString(USERNAME, username);
        editor.putString(PROFILEPHOTO, profilePicturePath);
        editor.apply();
    }
}
