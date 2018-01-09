package com.example.murat.gezi_yorum;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MediaPagerAdapter;

import java.util.ArrayList;
import java.util.Scanner;

public class MediaActivity extends AppCompatActivity {
    private ArrayList<MediaFile> mediaFiles;
    private LocationDbOpenHelper helper;
    private ViewPager viewPager;
    private Long trip_id;
    private MediaPagerAdapter mediaPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Bundle extras = getIntent().getExtras();
        viewPager = findViewById(R.id.viewpager);
        helper = new LocationDbOpenHelper(this);
        trip_id = extras.getLong(Trip.TRIPID, -1);
        if(trip_id != -1){
            mediaFiles = helper.getMediaFiles(trip_id, MediaFile.PHOTO, null, null);
            FloatingActionButton ok_button = findViewById(R.id.ok);
            ok_button.setVisibility(View.VISIBLE);
            ok_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    helper.updateTripCoverPhoto(trip_id, mediaFiles.get(viewPager.getCurrentItem()).id);
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });
        }else {
            ArrayList<Long> mediaIds = new ArrayList<>();
            String ids = extras.getString("fileIds", "");
            Scanner scanner = new Scanner(ids);
            while (scanner.hasNext()) {
                mediaIds.add(scanner.nextLong());
            }
            mediaFiles = new ArrayList<>();
            for (Long id : mediaIds) {
                mediaFiles.add(helper.getMediaFile(id));
            }
        }
        mediaPagerAdapter = new MediaPagerAdapter(getSupportFragmentManager(), mediaFiles);
        viewPager.setAdapter(mediaPagerAdapter);
    }
    public void removeFragment(long id){
        for(int i = 0; i<mediaFiles.size(); i++){
            if(mediaFiles.get(i).id == id){
                mediaFiles.remove(i);
                if(mediaFiles.size() == 0){
                    finish();
                    return;
                }
                mediaPagerAdapter.setDataset(mediaFiles);
                viewPager.setAdapter(mediaPagerAdapter);
                if(viewPager.getCurrentItem() >= mediaFiles.size()){
                    viewPager.setCurrentItem(mediaFiles.size()-1);
                }

            }
        }
    }
}
