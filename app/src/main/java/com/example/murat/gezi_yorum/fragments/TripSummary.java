package com.example.murat.gezi_yorum.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Abstract class merges TripInfo and ContiuningTrip
 */

public abstract class TripSummary extends Fragment {
    protected GridView photo_preview, video_preview, sound_preview, note_peview;
    protected ArrayList<MediaFile> photos;
    protected ArrayList<MediaFile> videos;
    protected ArrayList<MediaFile> sounds;
    protected ArrayList<MediaFile> notes;
    protected LocationDbOpenHelper helper;
    protected long trip_id;

    protected Polyline addedPolyLine;
    protected ArrayList<LatLng> points;

    /**
     * Seting up preview for media
     * @param view viev element
     */
    protected void setUpView(View view){
        try {
            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        photo_preview = view.findViewById(R.id.photo_preview_grid);
        video_preview = view.findViewById(R.id.video_preview_grid);
        sound_preview = view.findViewById(R.id.sound_preview_grid);
        note_peview = view.findViewById(R.id.note_preview_grid);

        photos = helper.getMediaFiles(trip_id, Constants.PHOTO,"LIMIT 5");
        videos = helper.getMediaFiles(trip_id, Constants.VIDEO,"LIMIT 5");
        sounds = helper.getMediaFiles(trip_id, Constants.SOUNDRECORD,"LIMIT 5");
        notes = helper.getMediaFiles(trip_id, Constants.NOTE,"LIMIT 5");

        photo_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                photos.get(position).startActivityForView(getActivity());
            }
        });
        video_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               videos.get(position).startActivityForView(getActivity());
            }
        });
        sound_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        note_peview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        setUpPreview();

        Button photo_more = view.findViewById(R.id.photo_more);
        Button video_more = view.findViewById(R.id.video_more);
        Button sound_more = view.findViewById(R.id.sound_more);
        Button note_more = view.findViewById(R.id.note_more);
        photo_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaActivity(Constants.PHOTO);
            }
        });
        video_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaActivity(Constants.VIDEO);
            }
        });
        sound_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaActivity(Constants.SOUNDRECORD);
            }
        });
        note_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaActivity(Constants.NOTE);
            }
        });
    }
    protected void setUpPreview(){
        photo_preview.setAdapter(new MediaGridViewAdapter(getContext(), photos));
        video_preview.setAdapter(new MediaGridViewAdapter(getContext(), videos));
        sound_preview.setAdapter(new MediaGridViewAdapter(getContext(), sounds));
        note_peview.setAdapter(new MediaGridViewAdapter(getContext(), notes));
    }

    /**
     * Returns related array by specified type
     * @param type type
     * @return array
     */
    protected ArrayList<MediaFile> getRelatedArray(String type){
        ArrayList<MediaFile> relatedArray = null;
        switch (type) {
            case Constants.PHOTO:
                relatedArray = photos;
                break;
            case Constants.VIDEO:
                relatedArray = videos;
                break;
            case Constants.SOUNDRECORD:
                relatedArray = sounds;
                break;
            case Constants.NOTE:
                relatedArray = notes;
                break;
        }
        return relatedArray;
    }

    /**
     * Adding markers to map
     * @param map Google Map
     */
    public void addMarkersToMap(GoogleMap map){
        for (MediaFile file : helper.getMediaFiles(trip_id,null,null)){
            file.addToMap(map);
        }
    }
    private void startMediaActivity(String actionType){
        Intent intent = new Intent(getContext(),MediaActivity.class);
        intent.putExtra(Constants.ACTION,actionType);
        intent.putExtra(Constants.TRIPID,trip_id);
        startActivity(intent);
    }

    /**
     * Draws trip path on map
     * @param map Google Map
     * @param move Move if true, else animate
     */

    abstract public void drawPathOnMap(GoogleMap map ,boolean move);
}
