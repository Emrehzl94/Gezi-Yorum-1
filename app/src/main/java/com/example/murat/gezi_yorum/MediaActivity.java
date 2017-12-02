package com.example.murat.gezi_yorum;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MediaPagerAdapter;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        long[] mediaIds = getIntent().getExtras().getLongArray("fileIds");

        LocationDbOpenHelper helper = new LocationDbOpenHelper(this);
        ArrayList<MediaFile> mediaFiles = new ArrayList<>();
        for (long id : mediaIds){
            mediaFiles.add(helper.getMediaFile(id));
        }
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MediaPagerAdapter(getSupportFragmentManager(), mediaFiles));

    }
}
