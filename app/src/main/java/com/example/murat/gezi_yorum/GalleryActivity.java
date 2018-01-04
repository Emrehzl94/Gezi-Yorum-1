package com.example.murat.gezi_yorum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MediaGridViewAdapter;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    ArrayList<MediaFile> mediaFiles;
    private long trip_id;
    GridView gridView;
    LocationDbOpenHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        trip_id = getIntent().getExtras().getLong(Trip.TRIPID);

        helper = new LocationDbOpenHelper(this);

        gridView = findViewById(R.id.preview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mediaFiles.get(i).startActivityForView(GalleryActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaFiles = helper.getMediaFiles(trip_id, null, null, null);
        MediaGridViewAdapter adapter = new MediaGridViewAdapter(this, mediaFiles);
        gridView.setAdapter(adapter);
    }
}
