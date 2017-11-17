package com.example.murat.gezi_yorum;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {
    ArrayList<MediaFile> files;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_watch_activity);
        GridView mediaView = (GridView) findViewById(R.id.media_view);

        Bundle extras = getIntent().getExtras();
        String action = extras.getString(Constants.ACTION);
        Long trip_id = extras.getLong(Constants.TRIPID);

        LocationDbOpenHelper helper = new LocationDbOpenHelper(this);
        files = helper.getMediaFiles(trip_id,action,"");
        mediaView.setAdapter(new MediaGridViewAdapter(this, files));
        mediaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                files.get(position).startActivityForView(MediaActivity.this);
            }
        });
    }
}
