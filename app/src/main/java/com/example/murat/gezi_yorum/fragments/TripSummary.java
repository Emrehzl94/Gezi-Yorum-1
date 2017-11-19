package com.example.murat.gezi_yorum.fragments;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.murat.gezi_yorum.GalleryActivity;
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
    protected GridView photo_preview, video_preview, sound_preview;
    protected ArrayList<MediaFile> photos;
    protected ArrayList<MediaFile> videos;
    protected ArrayList<MediaFile> sounds;
    protected LocationDbOpenHelper helper;
    protected long trip_id;

    protected GoogleMap map;
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

        photo_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ((MediaGridViewAdapter)photo_preview.getAdapter()).itemOnClick(position);
            }
        });
        video_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ((MediaGridViewAdapter)video_preview.getAdapter()).itemOnClick(position);
            }
        });
        sound_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ((MediaGridViewAdapter)sound_preview.getAdapter()).itemOnClick(position);
            }
        });

        new Runnable() {
            @Override
            public void run() {
                photos = helper.getMediaFiles(trip_id, Constants.PHOTO,"LIMIT 5");
                videos = helper.getMediaFiles(trip_id, Constants.VIDEO,"LIMIT 5");
                sounds = helper.getMediaFiles(trip_id, Constants.SOUNDRECORD,"LIMIT 5");
                setUpPreview();
            }
        }.run();

        Button photo_more = view.findViewById(R.id.photo_more);
        Button video_more = view.findViewById(R.id.video_more);
        Button sound_more = view.findViewById(R.id.sound_more);
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

    }
    protected void setUpPreview(){
        photo_preview.setAdapter(new MediaGridViewAdapter(getActivity(), photos));
        video_preview.setAdapter(new MediaGridViewAdapter(getActivity(), videos));
        sound_preview.setAdapter(new MediaGridViewAdapter(getActivity(), sounds));
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
        }
        return relatedArray;
    }

    /**
     * Adding markers to map
     */
    public void addMarkersToMap(){
        for (MediaFile file : helper.getMediaFiles(trip_id,null,null)){
            file.addToMap(map);
        }
    }
    private void startMediaActivity(String actionType){
        Intent intent = new Intent(getContext(),GalleryActivity.class);
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
