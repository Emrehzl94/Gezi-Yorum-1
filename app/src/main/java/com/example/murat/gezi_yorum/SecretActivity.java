package com.example.murat.gezi_yorum;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.murat.gezi_yorum.Entity.Constants;

public class SecretActivity extends AppCompatActivity {
    private EditText ip_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret);

        ip_address = findViewById(R.id.ip_address);
        ip_address.setText(Constants.ROOT);
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.ROOT = ip_address.getText().toString();
                Constants.APP = Constants.ROOT + "/Geziyorum/";
                SharedPreferences preferences = getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
                preferences.edit().putString("root",Constants.ROOT).apply();
            }
        });
    }
}
