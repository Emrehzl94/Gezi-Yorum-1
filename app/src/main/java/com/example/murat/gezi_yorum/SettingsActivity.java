package com.example.murat.gezi_yorum;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup share_options_radio;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.action_settings);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);

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
                editor.apply();
                Snackbar.make(view, getString(R.string.saved), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
