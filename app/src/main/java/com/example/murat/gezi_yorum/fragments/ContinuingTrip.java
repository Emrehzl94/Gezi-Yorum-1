package com.example.murat.gezi_yorum.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.LocationSaveService;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;


public class ContinuingTrip extends TripSummary implements OnMapReadyCallback, LocationSource, LocationListener {

    private static final int EXTERNAL_STORAGE_CAMERA_PERMISSION = 1;
    private static final int EXTERNAL_STORAGE_VIDEO_PERMISSION = 2;
    private static final int EXTERNAL_STORAGE_SOUNDRECORD_PERMISSION = 3;
    private static final int SOUNDRECORD_PERMISSION = 4;
    private static final int LOCATION_PERMISSION_REQUEST_ON_START = 5;
    private static final int LOCATION_PERMISSION_REQUEST_ON_CONTIUNE = 6;
    private static final int LOCATION_PERMISSION_REQUEST_ON_FAIL = 7;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_VIDEO_CAPTURE = 2;
    private int REQUEST_SOUND_RECORD= 3;

    private Uri lastOutputMedia;

    private BottomSheetBehavior behavior;
    private FloatingActionButton pause_continue;
    private View.OnClickListener pause, continue_listener;
    private MainActivity parentActivity;
    private OnLocationChangedListener listener;
    private LocationManager locationManager;

    private SharedPreferences preferences;

    private FloatingActionButton add_photo_fab;
    private FloatingActionButton add_video_fab;
    private FloatingActionButton add_sound_record_fab;

    private long path_id;
    private boolean isFabMenuOpen = false;
    private String state;
    public void setTrip_id(long trip_id) {
        this.trip_id = trip_id;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.continuing_trip_fragment, container, false);
        getActivity().setTitle("Devam eden");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ContinuingTrip.this);

        parentActivity = (MainActivity) getActivity();
        helper = new LocationDbOpenHelper(getContext());
        preferences = parentActivity.getPreferences(Context.MODE_PRIVATE);

        pause_continue = view.findViewById(R.id.pause_continue);

        trip_id = preferences.getLong(Constants.TRIPID,-1);
        path_id = preferences.getLong(Constants.PATH_ID, -1);
        state = preferences.getString(Constants.RECORDSTATE,Constants.PASSIVE);
        Bundle arguments = getArguments();
        if(arguments!=null) {
            String message = arguments.getString(Constants.MESSAGE);
            //This means coming from StartTripFragment
            if (message != null && message.equals(Constants.STARTNEWTRIP))
                startNewTrip();
        }
        FloatingActionButton add_fab = view.findViewById(R.id.add_media);
        add_photo_fab = view.findViewById(R.id.add_photo);
        add_video_fab = view.findViewById(R.id.add_video);
        add_sound_record_fab = view.findViewById(R.id.add_sound);
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


        FloatingActionButton stop = view.findViewById(R.id.stop);
        pause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPathRecording();
            }
        };
        continue_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPathRecording();
            }
        };
        if (state.equals(Constants.PASSIVE)) {
            pause_continue.setOnClickListener(continue_listener);
            pause_continue.setImageResource(R.drawable.aar_ic_play);
        } else {
            if(LocationSaveService.instance == null && checkLocationPermission(LOCATION_PERMISSION_REQUEST_ON_FAIL)){
                Intent intent = new Intent(getContext(), LocationSaveService.class);
                intent.putExtra(Constants.TRIPID,trip_id);
                intent.putExtra(Constants.PATH_ID,path_id);
                parentActivity.startService(intent);
            }
            pause_continue.setOnClickListener(pause);
            pause_continue.setImageResource(R.drawable.aar_ic_pause);
        }
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTrip();
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
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        if(map!=null){
            requestToDrawPathOnMap(map,true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(locationManager != null) {
            locationManager.removeUpdates(this);
            if (map != null) {
                map.clear();
            }
        }
    }

    public void startNewTrip(){
        if(!checkLocationPermission(LOCATION_PERMISSION_REQUEST_ON_START)) return;
        parentActivity.showSnackbarMessage("Trip started", Snackbar.LENGTH_LONG);
        trip_id = helper.startNewTrip();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.TRIPID,trip_id);
        editor.putString(Constants.TRIPSTATE, Constants.STARTED);
        editor.apply();
        startPathRecording();
    }
    public void endTrip(){
        parentActivity.showSnackbarMessage("Trip stopped", Snackbar.LENGTH_LONG);
        SharedPreferences.Editor editor = preferences.edit();
        stopPathRecording();
        helper.endTrip(trip_id);
        editor.putLong(Constants.TRIPID,-1);
        editor.putString(Constants.TRIPSTATE,Constants.ENDED);
        editor.apply();
    }
    public void startPathRecording(){
        if(!checkLocationPermission(LOCATION_PERMISSION_REQUEST_ON_CONTIUNE)) return;
        path_id = helper.startNewPath(trip_id);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PATH_ID,path_id);
        editor.putString(Constants.RECORDSTATE,Constants.ACTIVE);
        editor.apply();
        Intent intent = new Intent(getContext(),LocationSaveService.class);
        intent.putExtra(Constants.TRIPID,trip_id);
        intent.putExtra(Constants.PATH_ID,path_id);
        getActivity().startService(intent);
        pause_continue.setOnClickListener(pause);
        pause_continue.setImageResource(R.drawable.aar_ic_pause);
        //addPathOnMap(map, false, path_id);
    }
    public void stopPathRecording(){
        Intent intent = new Intent(getContext(),LocationSaveService.class);
        getActivity().stopService(intent);
        helper.endPath(path_id);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PATH_ID,-1);
        editor.putString(Constants.RECORDSTATE,Constants.PASSIVE);
        editor.apply();
        pause_continue.setOnClickListener(continue_listener);
        pause_continue.setImageResource(R.drawable.aar_ic_play);
    }

    private boolean checkLocationPermission(int request){
        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, request);
            return false;
        }
        return true;
    }

    private Marker lastClickedMarker;
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
                if(marker.getZIndex() != 1){
                    if(lastClickedMarker!=null){
                        lastClickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(markers.get(marker.getSnippet()).getColorForMap()));
                        lastClickedMarker.setZIndex(0);
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(markers.get(marker.getSnippet()).thumbNail));
                    marker.setZIndex(1);
                    lastClickedMarker = marker;
                }else {
                    helper.getMediaFile(Long.parseLong(marker.getTitle())).startActivityForView(getActivity());
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(markers.get(marker.getSnippet()).getColorForMap()));
                    marker.setZIndex(0);
                    lastClickedMarker = null;
                }
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
                if(lastClickedMarker!=null){
                    lastClickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(helper.getMediaFile(Long.parseLong(lastClickedMarker.getTitle())).getColorForMap()));
                    lastClickedMarker.setZIndex(0);
                }
            }
        });
        requestToDrawPathOnMap(map,true);

    }

    /**
     * Adding new point to polyline
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
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_CAMERA_PERMISSION)) return;
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        lastOutputMedia = getOutputMediaFile(Constants.PHOTO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Starts new photo intent for video
     */
    public void startNewVideoIntent() {
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_VIDEO_PERMISSION)) return;
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        lastOutputMedia = getOutputMediaFile(Constants.VIDEO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_VIDEO_CAPTURE);
    }

    /**
     * starts activity RecordAudio
     */
    public void startNewAudioIntent() {
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_SOUNDRECORD_PERMISSION) || !checkSoundRecordPermission()) return;
        int color = Color.CYAN;
        lastOutputMedia = getOutputMediaFile(Constants.SOUNDRECORD);
        AndroidAudioRecorder.with(this)
                .setFilePath(lastOutputMedia.getPath())
                .setColor(color)
                .setRequestCode(REQUEST_SOUND_RECORD)
                .recordFromFragment();
    }

    /**
     * Check for external storage permission. If permission is not granted request permission
     * @return true if permission granted
     */
    private boolean checkExternalStoragePermission(int request){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);
            return false;
        }
        return true;
    }

    private boolean checkSoundRecordPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, SOUNDRECORD_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            String type = "";
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                type = Constants.PHOTO;
            }else if(requestCode == REQUEST_VIDEO_CAPTURE){
                type = Constants.VIDEO;
            }else if(requestCode == REQUEST_SOUND_RECORD){
                type = Constants.SOUNDRECORD;
            }
            @SuppressLint("MissingPermission") Location lastknown = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String share_option = preferences.getString(Constants.SHARE_OPTION, Constants.EVERYBODY);
            MediaFile mediaFile = new MediaFile(type,lastOutputMedia.getPath(),lastknown.getLatitude(),lastknown.getLongitude(), lastknown.getAltitude(),trip_id,System.currentTimeMillis(), share_option);
            new Handler().post(new ThumbnailGeneration(mediaFile));
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                startNewPhotoIntent();
            }
        }
    }
    private Uri getOutputMediaFile(String type){
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)),MediaFile.getSubdir(type));
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        long time = System.currentTimeMillis();
        return Uri.parse("file://"+storageDir.getPath() + File.separator + time+"."+MediaFile.getExtension(type));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case EXTERNAL_STORAGE_CAMERA_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Depolama izni olmadan fotoğraf kaydedilemez", Snackbar.LENGTH_LONG);
                }else {
                    startNewPhotoIntent();
                }
                break;
            case EXTERNAL_STORAGE_VIDEO_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Depolama izni olmadan video kaydedilemez", Snackbar.LENGTH_LONG);
                }else {
                    startNewVideoIntent();
                }
                break;
            case EXTERNAL_STORAGE_SOUNDRECORD_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Depolama izni olmadan ses kaydedilemez.", Snackbar.LENGTH_LONG);
                }else {
                    startNewAudioIntent();
                }
                break;
            case SOUNDRECORD_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Ses kayıt izni olmadan ses kaydı yapılamaz.", Snackbar.LENGTH_LONG);
                }else {
                    startNewAudioIntent();
                }
                break;
            case LOCATION_PERMISSION_REQUEST_ON_START:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Konum izni olmadan konum kaydı yapılamaz", Snackbar.LENGTH_LONG);
                }else {
                    startNewTrip();
                }
                break;
            case LOCATION_PERMISSION_REQUEST_ON_CONTIUNE:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Konum izni olmadan konum kaydı yapılamaz", Snackbar.LENGTH_LONG);
                }else {
                    startPathRecording();
                }
                break;
            case LOCATION_PERMISSION_REQUEST_ON_FAIL:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Konum izni olmadan konum kaydı yapılamaz.", Snackbar.LENGTH_LONG);
                }else {
                    Intent intent = new Intent(getContext(), LocationSaveService.class);
                    intent.putExtra(Constants.TRIPID,trip_id);
                    intent.putExtra(Constants.PATH_ID,path_id);
                    parentActivity.startService(intent);
                }
                break;
        }

    }

    public class ThumbnailGeneration implements Runnable{
        private MediaFile mediaFile;

        ThumbnailGeneration(MediaFile mediaFile){
            this.mediaFile = mediaFile;
        }
        @Override
        public void run() {
            mediaFile.generateThumbNail(getActivity());
            mediaFiles.add(0,mediaFile);
            helper.insertMediaFile(mediaFile);
            setUpPreview();
        }
    }
}
