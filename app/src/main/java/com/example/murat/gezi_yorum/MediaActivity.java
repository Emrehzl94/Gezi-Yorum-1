package com.example.murat.gezi_yorum;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_watch_activity);
        GridView mediaView = (GridView) findViewById(R.id.media_view);
        ArrayList<Bitmap> media = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        String action = extras.getString(Constants.ACTION);
        Long trip_id = extras.getLong(Constants.TRIPID);

        LocationDbOpenHelper helper = new LocationDbOpenHelper(this);
        ArrayList<MediaFile> files = helper.getMediaFiles(trip_id,action,"");
        for(MediaFile file : files){
            media.add(file.getThumbnail(this));
        }
        mediaView.setAdapter(new MediaGridViewAdapter(this, media));
    }
}
