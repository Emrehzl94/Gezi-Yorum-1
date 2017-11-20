package com.example.murat.gezi_yorum.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.murat.gezi_yorum.GalleryActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.LocationCSVHandler;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class merges TripInfo and ContiuningTrip
 */

public abstract class TripSummary extends Fragment {
    protected GridView preview;
    protected ArrayList<MediaFile> mediaFiles;
    protected LocationDbOpenHelper helper;
    protected long trip_id;

    protected GoogleMap map;
    protected HashMap<String,MediaFile> markers;
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
        preview = view.findViewById(R.id.preview);
        new Runnable() {
            @Override
            public void run() {
               mediaFiles = helper.getMediaFiles(trip_id,null,null);
               preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                       mediaFiles.get(position).startActivityForView(getActivity());
                   }
               });
               setUpPreview();
            }
        }.run();


    }

    protected void setUpPreview(){
        preview.setAdapter(new MediaGridViewAdapter(getActivity(),mediaFiles));
    }

    /**
     * Adding markers to map
     */
    public void addMarkersToMap(GoogleMap map){
        markers = new HashMap<>();
        if(mediaFiles.size() == 0) return;
        MediaFile previous = mediaFiles.get(0);
        markers.put(previous.addToMap(map).getSnippet(),previous);
        for (MediaFile file : mediaFiles){
            if(!(previous.location.distInMeters(file.location) < 5)) {
                markers.put(file.addToMap(map).getSnippet(), file);
            }
            previous = file;
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

    public void drawPathOnMap(GoogleMap map ,boolean move){
        if (map != null) {
            map.clear();
            points = new LocationCSVHandler(trip_id,getContext()).getLocations();

            PolylineOptions options = new PolylineOptions();
            if (!points.isEmpty()) {
                options.color(Color.RED);
                options.width(15);
                options.visible(true);
                options.addAll(points);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : points) {
                    builder.include(point);
                }
                int routePadding = 200;
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), routePadding);
                if (move) {
                    map.moveCamera(update);
                } else {
                    map.animateCamera(update);
                }
                addedPolyLine = map.addPolyline(options);
                addMarkersToMap(map);
            }
        }
    }
}
