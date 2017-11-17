package com.example.murat.gezi_yorum.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.murat.gezi_yorum.LocationSaveService;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.NoteTake;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.RecordAudio;
import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.util.ArrayList;


public class ContinuingTrip extends TripSummary implements OnMapReadyCallback, LocationSource, LocationListener {

    private int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_VIDEO_CAPTURE = 2;
    private int REQUEST_SOUND_RECORD= 3;
    private int REQUEST_TAKE_NOTE= 4;

    private Uri lastOutputMedia;

    private BottomSheetBehavior behavior;
    private Button pause_continue;
    private View.OnClickListener pause, continue_listener;
    private MainActivity parentActivity;
    private OnLocationChangedListener listener;
    private GoogleMap map;
    private LocationManager locationManager;

    private FloatingActionButton add_photo_fab;
    private FloatingActionButton add_video_fab;
    private FloatingActionButton add_sound_record_fab;
    private FloatingActionButton add_note_fab;
    private boolean isFabMenuOpen = false;
    public void setTrip_id(long trip_id) {
        this.trip_id = trip_id;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.continuing_trip_fragment, container, false);

        helper = new LocationDbOpenHelper(getContext());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getActivity().setTitle("Devam eden");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton add_fab = view.findViewById(R.id.add_media);
        add_photo_fab = view.findViewById(R.id.add_photo);
        add_video_fab = view.findViewById(R.id.add_video);
        add_sound_record_fab = view.findViewById(R.id.add_sound);
        add_note_fab = view.findViewById(R.id.add_note);
        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float multiplier = -1;
                if(!isFabMenuOpen) {
                    isFabMenuOpen = true;
                }else {
                    isFabMenuOpen = false;
                    multiplier = 0;
                }
                float tx = add_photo_fab.getHeight() + 100;
                add_photo_fab.animate().translationY(multiplier*tx);
                tx += add_video_fab.getHeight() + 100;
                add_video_fab.animate().translationY(multiplier*tx);
                tx += add_sound_record_fab.getHeight() +100;
                add_sound_record_fab.animate().translationY(multiplier*tx);
                tx += add_note_fab.getHeight() + 100;
                add_note_fab.animate().translationY(multiplier*tx);
            }
        });
        add_photo_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewPhotoIntent();
            }
        });
        add_video_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewVideoIntent();
            }
        });
        add_sound_record_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAudioIntent();
            }
        });
        add_note_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewNoteIntent();
            }
        });

        pause_continue = view.findViewById(R.id.pause_continue);
        Button stop = view.findViewById(R.id.stop);
        parentActivity = (MainActivity) getActivity();
        pause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.stopRecording();
                pause_continue.setOnClickListener(continue_listener);
                pause_continue.setText("Contiune");
                pause_continue.setBackgroundColor(Color.GREEN);
            }
        };
        continue_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.startRecording();
                pause_continue.setOnClickListener(pause);
                pause_continue.setText("Pause");
                pause_continue.setBackgroundColor(Color.YELLOW);
            }
        };
        if (LocationSaveService.instance == null) {
            pause_continue.setOnClickListener(continue_listener);
            pause_continue.setText("Contiune");
            pause_continue.setBackgroundColor(Color.GREEN);
        } else {
            pause_continue.setOnClickListener(pause);
            pause_continue.setText("Pause");
            pause_continue.setBackgroundColor(Color.YELLOW);
        }
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.endTrip();
            }
        });

        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setState(BottomSheetBehavior.STATE_DRAGGING);
        behavior.setHideable(false);
        behavior.setPeekHeight(300);

        setUpView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        if(map!=null){
            drawPathOnMap(map,true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        map.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = googleMap;
        map.setLocationSource(this);
        map.setMyLocationEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behavior.setHideable(false);
                map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(behavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    behavior.setHideable(false);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else {
                    behavior.setHideable(true);
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        drawPathOnMap(map,true);

    }

    /**
     * Draws path on map
     * @param map Google Map
     * @param move Move if true, else animate
     */
    public void drawPathOnMap(GoogleMap map ,boolean move) {
        if (map != null) {
            map.clear();
            points = helper.getTripPath(trip_id);

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

    /**
     * Adding new point to polyline
     * @param location provided location
     */
    @Override
    public void onLocationChanged(Location location) {
        if(listener != null){
            listener.onLocationChanged(location);
            if(addedPolyLine != null && points != null){
                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
                addedPolyLine.setPoints(points);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        listener = null;
    }


    /**
     * starts new photo intent for taking photo
     */
    public void startNewPhotoIntent() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        lastOutputMedia = getOutpuMediaFile(Constants.PHOTO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Starts new photo intent for video
     */
    public void startNewVideoIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        lastOutputMedia = getOutpuMediaFile(Constants.VIDEO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_VIDEO_CAPTURE);
    }

    /**
     * starts activity RecordAudio
     */
    public void startNewAudioIntent() {
        Intent audioIntent = new Intent(getContext(), RecordAudio.class);
        audioIntent.putExtra(MediaStore.EXTRA_OUTPUT,getOutpuMediaFile(Constants.SOUNDRECORD));
        startActivityForResult(audioIntent, REQUEST_SOUND_RECORD);
    }

    /**
     * start activity NateTake
     */
    public void startNewNoteIntent(){
        
        Intent noteIntent = new Intent(getContext(), NoteTake.class);
        startActivityForResult(noteIntent, REQUEST_TAKE_NOTE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            String type = "";
            Uri outputMedia = null;
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                type = Constants.PHOTO;
                outputMedia = lastOutputMedia;
                startNewPhotoIntent();
            }else if(requestCode == REQUEST_VIDEO_CAPTURE){
                outputMedia = lastOutputMedia;
                type = Constants.VIDEO;
            }else if(requestCode == REQUEST_SOUND_RECORD){
                outputMedia = lastOutputMedia;
                type = Constants.SOUNDRECORD;
            }else if(requestCode == REQUEST_TAKE_NOTE){
                outputMedia = lastOutputMedia;
                type = Constants.NOTE;
            }
            @SuppressLint("MissingPermission") Location lastknown = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            MediaFile mediaFile = new MediaFile(type,outputMedia.getPath(),lastknown.getLatitude(),lastknown.getLongitude(), lastknown.getAltitude(),trip_id,System.currentTimeMillis());
            mediaFile.generateThumbNail(getActivity());
            helper.insertMediaFile(mediaFile);
            ArrayList<MediaFile> relatedArray = getRelatedArray(type);
            relatedArray.add(0,mediaFile);
            if(relatedArray.size()>5){
                relatedArray.remove(5);
            }
        }else {
            setUpPreview();
        }

    }
    private Uri getOutpuMediaFile(String type){
        String subdir = "";
        String extension = "";
        switch (type) {
            case Constants.PHOTO:
                subdir = "Photos";
                extension = "jpg";
                break;
            case Constants.VIDEO:
                subdir = "Videos";
                extension = "mp4";
                break;
            case Constants.SOUNDRECORD:
                subdir = "Audio";
                extension = "mp3";
                break;
            case Constants.NOTE:
                subdir = "Notes";
                extension = "txt";
                break;
        }
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)),subdir);
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        long time = System.currentTimeMillis();
        return Uri.parse("file://"+storageDir.getPath() + File.separator + time+"."+extension);
    }
}
