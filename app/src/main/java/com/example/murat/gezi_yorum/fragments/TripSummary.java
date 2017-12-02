package com.example.murat.gezi_yorum.fragments;

import android.graphics.Color;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationCSVHandler;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MediaGridViewAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Abstract class merges TripInfo and ContiuningTrip
 */

public abstract class TripSummary extends Fragment {
    protected GridView preview;
    protected ArrayList<MediaFile> mediaFiles;
    protected LocationDbOpenHelper helper;
    protected long trip_id;
    protected String name;

    protected GoogleMap map;
    protected Polyline addedPolyLine;
    protected ArrayList<LatLng> points;

    protected boolean preview_setted = false;
    /**
     * Seting up preview for media
     * @param view view element
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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mediaFiles = helper.getMediaFiles(trip_id,null,null);
                preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        MediaFile file = mediaFiles.size()>position ?  mediaFiles.get(position) : null;
                        if(file != null)
                            file.startActivityForView(getActivity());
                    }
                });
                setUpPreview();
                preview_setted = true;
            }
        });


    }

    protected void setUpPreview(){
        preview.setAdapter(new MediaGridViewAdapter(getActivity(),mediaFiles));
    }

    /**
     * Adding markers to map
     */
    public void addMarkersToMap(GoogleMap map){
        if(mediaFiles ==null || mediaFiles.size() == 0) return;
        MediaFile previous = null; // previous media file
        ArrayList<Long> mediaGroup = new ArrayList<>();
        for(MediaFile file : mediaFiles){
            // İf distance between previous file is lower than 5 meters add this file to media group
            if(previous == null || (previous.location.distInMeters(file.location)<5)){
                mediaGroup.add(file.id);
            }else {
                // else if media group is empty add this file to map
                if(mediaGroup.size() == 0){
                    previous.addToMap(map);
                    mediaGroup.clear();
                }else {
                    //if media group has at least one file, including current file two files add these files
                    //to map as media group
                    String snippet =file.id + " ";
                    for (Long id : mediaGroup){
                        snippet += id +" ";
                    }
                    map.addMarker(new MarkerOptions().position(previous.location.convertLatLng())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            .snippet(String.valueOf(snippet)));
                    mediaGroup.clear();
                }
            }
            previous = file;
        }
        // if media group has only last file add last file to map
        if(mediaGroup.size() == 1){
            previous.addToMap(map);
        }else if(mediaGroup.size() > 1) {
            // else if media group has more than one file add these files to map as media group
            String snippet = "";
            for (Long id : mediaGroup){
                snippet += id +" ";
            }
            map.addMarker(new MarkerOptions().position(previous.location.convertLatLng())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet(String.valueOf(snippet)));
        }
    }

    /**
     * Handles drawing sequence because path cannot be drawen on the map before media files loaded
     * @param googleMap GoogleMap
     * @param move move map or animate
     */
    public void requestToDrawPathOnMap(GoogleMap googleMap, final boolean move){
        if(googleMap == null) return;
        this.map = googleMap;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(map != null) {
                    while (!preview_setted){
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    map.clear();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    int routePadding = 200;
                    for (long path_id : helper.getPathsIDs(trip_id)) {
                        addPathOnMap(map, move, path_id);
                        for (LatLng point : points) {
                            builder.include(point);
                        }
                    }
                    try {
                        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), routePadding);
                        if (move) {
                            map.moveCamera(update);
                        } else {
                            map.animateCamera(update);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Draws trip path on map, adds new polyline
     * @param map Google Map
     * @param move Move if true, else animate
     */

    protected void addPathOnMap(GoogleMap map , boolean move, long path_id){
        points = new LocationCSVHandler(trip_id, path_id,getContext()).getLocations();

        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(15);
        options.visible(true);
        options.addAll(points);
        addedPolyLine = map.addPolyline(options);
        addMarkersToMap(map);
    }
}
