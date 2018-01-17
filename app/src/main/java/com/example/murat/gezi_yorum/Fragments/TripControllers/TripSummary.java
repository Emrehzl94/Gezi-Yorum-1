package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.R;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class merges TripInfo and ContiuningTrip
 */

public abstract class TripSummary extends Fragment {
    protected GridView preview;
    protected ArrayList<MediaFile> mediaFiles;
    protected LocationDbOpenHelper helper;
    protected Trip trip;

    protected TextView header;
    protected ImageButton edit_name;
    protected GoogleMap map;
    protected Polyline addedPolyLine;
    protected List<LatLng> points;

    protected Handler handler;
    protected Path active_path;
    protected boolean preview_setted = false;

    private Integer limit;
    /**
     * Seting up preview for media
     * @param view view element
     */
    protected void setUpView(View view){
        try {
            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        header = view.findViewById(R.id.header);
        header.setText(trip.name);
        edit_name = view.findViewById(R.id.edit_name);
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final EditText editName = new EditText(getContext());
                editName.setId(0);
                editName.setText(trip.name);
                builder.setView(editName);
                builder.setTitle(getString(R.string.change_name));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String new_name = editName.getText().toString();
                        helper.updateTripName(trip.id, new_name);
                        header.setText(new_name);
                        trip.name = new_name;
                    }
                });
                builder.create();
                builder.show();
            }
        });

        preview = view.findViewById(R.id.preview);
        handler = new Handler();
        Bundle arguments = getArguments();
        if(arguments != null) {
            limit = getArguments().getInt("limit", -1);
            if(limit != -1) {
                limit = null;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaFiles = helper.getMediaFiles(trip.id,null, null, limit, false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
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
        }).start();
    }

    protected void setUpPreview(){
        Activity activity = getActivity();
        if(activity != null)
            preview.setAdapter(new MediaGridViewAdapter(getActivity(),mediaFiles));
    }

    /**
     * Adding markers to map
     */
    @SuppressWarnings("ConstantConditions")
    public void addMarkersToMap(GoogleMap map, List<MediaFile> mediaFiles, Boolean isImported){
        if(mediaFiles ==null || mediaFiles.size() == 0) return;
        float color = isImported ? BitmapDescriptorFactory.HUE_BLUE : BitmapDescriptorFactory.HUE_YELLOW;
        MediaFile previous = null; // previous media file
        ArrayList<Long> mediaGroup = new ArrayList<>();
        for(MediaFile file : mediaFiles){
            // İf distance between previous file is lower than 5 meters add this file to media group
            if(previous == null || (previous.location.distInMeters(file.location)<5)){
                mediaGroup.add(file.id);
            }else {
                // else if media group is empty add this file to map
                if(mediaGroup.size() == 1){
                    previous.addToMap(map, isImported);
                    mediaGroup.clear();
                    mediaGroup.add(file.id);
                }else {
                    //if media group has at least one file, including current file two files add these files
                    //to map as media group
                    StringBuilder snippetBuilder = new StringBuilder();
                    for (Long id : mediaGroup){
                        snippetBuilder.append(id);
                        snippetBuilder.append(" ");
                    }
                    String snippet = snippetBuilder.toString();
                    map.addMarker(new MarkerOptions().position(previous.location.convertLatLng())
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                            .title(String.valueOf(mediaGroup.size()))
                            .snippet(String.valueOf(snippet)));
                    mediaGroup.clear();
                    mediaGroup.add(file.id);
                }
            }
            previous = file;
        }
        // if media group has only last file add last file to map
        if(mediaGroup.size() == 1){
            previous.addToMap(map, isImported);
        }else if(mediaGroup.size() > 1) {
            // else if media group has more than one file add these files to map as media group
            StringBuilder snippetBuilder = new StringBuilder();
            for (Long id : mediaGroup){
                snippetBuilder.append(id);
                snippetBuilder.append(" ");
            }
            String snippet = snippetBuilder.toString();
            map.addMarker(new MarkerOptions().position(previous.location.convertLatLng())
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .snippet(String.valueOf(snippet)));
        }
    }

    /**
     * Handles drawing sequence because path cannot be drawen on the map before media files loaded
     * @param googleMap GoogleMap
     * @param move move map or animate
     */
    public void requestToDrawPathOnMap(GoogleMap googleMap, boolean move, Trip followingTrip){
        if(googleMap == null) return;
        this.map = googleMap;
        map.clear();
        if(followingTrip != null){
            new Thread(new TripDrawer(followingTrip, move)).start();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new Thread(new TripDrawer(trip, move)).start();
    }
    /**
     * Controls trip drawing on map can draw any trip
     */
    protected class TripDrawer implements Runnable {
        private Trip trip;
        private Boolean move;
        TripDrawer(Trip trip, Boolean move){
            this.trip = trip;
            this.move = move;
        }

        @Override
        public void run() {
            ArrayList<Long> pathIds = helper.getPathsIDs(trip.id);
            if(map != null && !pathIds.isEmpty()) {
                //waiting for media files
                while (!preview_setted){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(new Runnable() {
                    private ArrayList<Long> pathIds;
                    Runnable setPathIds(ArrayList<Long> pathIds){this.pathIds = pathIds; return this;}
                    @Override
                    public void run() {

                        for (long path_id : pathIds) {
                            if(!trip.isImported){
                                active_path = helper.getPath(path_id);
                                points = active_path.getLocationsAsLatLng();
                                addedPolyLine = active_path.drawOnMap(map, points, trip.isImported);
                                addMarkersToMap(map, mediaFiles, trip.isImported);
                                points = addedPolyLine.getPoints();
                                updateAndAnimateMap(map, points, move);
                            }else {
                                Path path = helper.getPath(path_id);
                                ArrayList<MediaFile> media = helper.getMediaFiles(trip.id, null, null, null, false);
                                addMarkersToMap(map, media, trip.isImported);
                                updateAndAnimateMap(map, path.drawOnMap(map, path.getLocationsAsLatLng(), trip.isImported).getPoints(), move);
                            }
                        }
                    }
                }.setPathIds(pathIds));
            }
        }
    }

    private void updateAndAnimateMap(GoogleMap map, List<LatLng> points, Boolean move){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        try {
            int routePadding = 200;
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
