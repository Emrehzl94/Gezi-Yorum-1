package com.example.murat.gezi_yorum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.User;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup share_options_radio;
    Switch live_track;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.action_settings);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        User user = new User(preferences);
        preferences = getSharedPreferences(Constants.PREFNAME + user.username, Context.MODE_PRIVATE);

        share_options_radio = findViewById(R.id.share_options_radio);
        int selected = 0;
        switch (preferences.getString(MediaFile.SHARE_OPTION,MediaFile.EVERYBODY)){
            case MediaFile.EVERYBODY:
                selected = R.id.everybody;
                break;
            case MediaFile.MY_FRIENDS:
                selected = R.id.my_friends;
                break;
            case MediaFile.ONLY_ME:
                selected = R.id.only_me;
        }
        share_options_radio.check(selected);

        live_track = findViewById(R.id.live_track);
        live_track.setChecked(preferences.getBoolean(Constants.LIVE_TRACK, true));

        Button save_button = findViewById(R.id.save);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = share_options_radio.getCheckedRadioButtonId();
                String option = "";
                switch (selectedId){
                    case R.id.everybody:
                        option = MediaFile.EVERYBODY;
                        break;
                    case R.id.my_friends:
                        option = MediaFile.MY_FRIENDS;
                        break;
                    case R.id.only_me:
                        option = MediaFile.ONLY_ME;
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(MediaFile.SHARE_OPTION, option);
                editor.putBoolean(Constants.LIVE_TRACK, live_track.isChecked());
                editor.apply();
                Snackbar.make(view, getString(R.string.saved), Snackbar.LENGTH_LONG).show();
            }
        });

        save_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, SecretActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
