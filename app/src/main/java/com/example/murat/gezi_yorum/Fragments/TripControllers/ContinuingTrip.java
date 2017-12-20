package com.example.murat.gezi_yorum.Fragments.TripControllers;

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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import com.afollestad.materialcamera.MaterialCamera;
import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.LocationSaveService;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;


public class ContinuingTrip extends TripSummary implements OnMapReadyCallback, LocationSource, LocationListener {

    private static final int EXTERNAL_STORAGE_CAMERA_PERMISSION = 1;
    private static final int EXTERNAL_STORAGE_VIDEO_PERMISSION = 2;
    private static final int EXTERNAL_STORAGE_SOUNDRECORD_PERMISSION = 3;
    private static final int SOUNDRECORD_PERMISSION = 4;
    private static final int MIC_CAMERA_PERMISSION = 5;
    private static final int PHOTO_CAMERA_PERMISSION = 6;
    private static final int MIC_VIDEO_PERMISSION = 7;
    private static final int VIDEO_CAMERA_PERMISSION = 8;
    private static final int LOCATION_PERMISSION_REQUEST = 9;
    private static final int LOCATION_PERMISSION_REQUEST_ON_FAIL = 10;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_VIDEO_CAPTURE = 2;
    private int REQUEST_SOUND_RECORD= 3;

    private String outputFile;

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

    private Trip followingTrip;
    private long path_id;
    private boolean isFabMenuOpen = false;

    Handler activityHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.continuing_trip_fragment, container, false);
        getActivity().setTitle("Devam eden");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ContinuingTrip.this);

        parentActivity = (MainActivity) getActivity();
        helper = new LocationDbOpenHelper(getContext());
        preferences = parentActivity.getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        activityHandler = new Handler();

        pause_continue = view.findViewById(R.id.pause_continue);

        String state = preferences.getString(Trip.RECORDSTATE, Trip.PASSIVE);
        Bundle arguments = getArguments();
        if(arguments!=null) {
            String message = arguments.getString(Constants.MESSAGE);
            //This means coming from StartTripFragment
            if (message != null && message.equals(Constants.STARTNEWTRIP)) {
                Boolean isCreator = arguments.getBoolean(Trip.CREATOR, true);
                startNewTrip(arguments.getString(Trip.TRIPNAME),
                        arguments.getString(Trip.MEMBERS),
                        arguments.getLong(Constants.TRIPIDONSERVER), isCreator);
                Long choosen_id = arguments.getLong(Constants.CHOSEN_TRIPID);
                if(choosen_id != 0) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong(Constants.CHOSEN_TRIPID, choosen_id);
                    editor.apply();
                    followingTrip = helper.getTrip(choosen_id);
                }
            }
        }
        if(trip == null){
            Long trip_id = preferences.getLong(Trip.TRIPID,-1);
            path_id = preferences.getLong(Path.PATH_ID, -1);
            trip = helper.getTrip(trip_id);
        }
        if(followingTrip == null){
            Long choosen_id = preferences.getLong(Constants.CHOSEN_TRIPID, -1);
            if(choosen_id != -1)
                followingTrip = helper.getTrip(choosen_id);
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
        if (state.equals(Trip.PASSIVE)) {
            pause_continue.setOnClickListener(continue_listener);
            pause_continue.setImageResource(R.drawable.aar_ic_play);
        } else {
            if(LocationSaveService.instance == null && checkLocationPermission(LOCATION_PERMISSION_REQUEST_ON_FAIL)){
                Intent intent = new Intent(getContext(), LocationSaveService.class);
                intent.putExtra(Path.PATH_ID,path_id);
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
            //noinspection ConstantConditions
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        if(map!=null){
            if(followingTrip != null){
                new Thread(new TripDrawer(followingTrip, null)).start();
            }
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

    public void startNewTrip(String name, String members, Long idOnServer, Boolean isCreator){
        trip = helper.startNewTrip(name, members, idOnServer, isCreator);
        parentActivity.showSnackbarMessage(getString(R.string.trip_created), Snackbar.LENGTH_LONG);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Trip.TRIPID, trip.id);
        editor.putString(Trip.TRIPSTATE, Trip.STARTED);
        editor.apply();
    }
    public void endTrip(){
        parentActivity.showSnackbarMessage(getString(R.string.trip_finished), Snackbar.LENGTH_LONG);
        SharedPreferences.Editor editor = preferences.edit();
        stopPathRecording();
        helper.endTrip(trip.id);
        editor.putLong(Trip.TRIPID,-1);
        editor.putString(Trip.TRIPSTATE, Trip.ENDED);
        editor.putLong(Constants.CHOSEN_TRIPID, -1);
        editor.apply();
    }
    public void startPathRecording(){
        if(!checkLocationPermission(LOCATION_PERMISSION_REQUEST)) return;
        long path_id = helper.startNewPath(trip.id, getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Path.PATH_ID,path_id);
        editor.putString(Trip.RECORDSTATE,Trip.ACTIVE);
        editor.apply();
        Intent intent = new Intent(getContext(),LocationSaveService.class);
        intent.putExtra(Path.PATH_ID,path_id);
        getActivity().startService(intent);
        pause_continue.setOnClickListener(pause);
        pause_continue.setImageResource(R.drawable.aar_ic_pause);
        //addPathOnMap(map, path_id);
    }
    public void stopPathRecording(){
        Intent intent = new Intent(getContext(),LocationSaveService.class);
        getActivity().stopService(intent);
        helper.endPath(path_id);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Path.PATH_ID,-1);
        editor.putString(Trip.RECORDSTATE,Trip.PASSIVE);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = googleMap;
        if(followingTrip != null){
            new Thread(new TripDrawer(followingTrip, null)).start();
        }
        map.setLocationSource(this);
        map.setMyLocationEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getContext(), MediaActivity.class);
                intent.putExtra("fileIds",marker.getSnippet());
                parentActivity.startActivity(intent);
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
        requestToDrawPathOnMap(map,true);
    }

    /**
     * Adding new point to polyline
     */
    @Override
    public void onLocationChanged(Location location) {
        //if(location.getAccuracy()>4) return;
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
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_CAMERA_PERMISSION)
                || !checkSoundRecordPermission(MIC_CAMERA_PERMISSION)
                || !checkCameraPermission(PHOTO_CAMERA_PERMISSION)) return;
        String outputDir = getOutputMediaFileDir(MediaFile.PHOTO);
        new MaterialCamera(this)
                .labelRetry(R.string.cancel)
                .labelConfirm(R.string.save)
                .stillShot()
                .saveDir(outputDir)
                .start(REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Starts new photo intent for video
     */
    public void startNewVideoIntent() {
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_VIDEO_PERMISSION)
                || !checkSoundRecordPermission(MIC_VIDEO_PERMISSION)
                || !checkCameraPermission(VIDEO_CAMERA_PERMISSION)) return;
        String outputDir = getOutputMediaFileDir(MediaFile.VIDEO);
        new MaterialCamera(this)
                .labelRetry(R.string.cancel)
                .labelConfirm(R.string.save)
                .saveDir(outputDir)
                .showPortraitWarning(false)
                .start(REQUEST_VIDEO_CAPTURE);
    }

    /**
     * starts activity RecordAudio
     */
    public void startNewAudioIntent() {
        if(!checkExternalStoragePermission(EXTERNAL_STORAGE_SOUNDRECORD_PERMISSION) || !checkSoundRecordPermission(SOUNDRECORD_PERMISSION)) return;
        int color = Color.CYAN;
        String outputDir = getOutputMediaFileDir(MediaFile.SOUNDRECORD);
        outputFile = outputDir + File.separator + System.currentTimeMillis()
                +"."+MediaFile.getExtension(MediaFile.SOUNDRECORD);
        AndroidAudioRecorder.with(this)
                .setFilePath(outputFile)
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

    private boolean checkSoundRecordPermission(int request){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, request);
            return false;
        }
        return true;
    }

    private boolean checkCameraPermission(int request){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, request);
            return false;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            String type = "";
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                outputFile = data.getData().getPath();
                type = MediaFile.PHOTO;
            }else if(requestCode == REQUEST_VIDEO_CAPTURE){
                outputFile = data.getData().getPath();
                type = MediaFile.VIDEO;
            }else if(requestCode == REQUEST_SOUND_RECORD){
                type = MediaFile.SOUNDRECORD;
            }
            String share_option = preferences.getString(MediaFile.SHARE_OPTION, MediaFile.EVERYBODY);
            new Thread(new ThumbnailGeneration(outputFile, type, share_option)).start();
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                startNewPhotoIntent();
            }
        }
    }
    private String getOutputMediaFileDir(String type){
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)),MediaFile.getSubdir(type));
        if(!storageDir.exists()){
            //noinspection ResultOfMethodCallIgnored
            storageDir.mkdirs();
        }
        return storageDir.getPath();
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
            case MIC_CAMERA_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Ses kayıt izni olmadan ses kaydı yapılamaz.", Snackbar.LENGTH_LONG);
                }else {
                    startNewPhotoIntent();
                }
                break;
            case MIC_VIDEO_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Ses kayıt izni olmadan ses kaydı yapılamaz.", Snackbar.LENGTH_LONG);
                }else {
                    startNewVideoIntent();
                }
                break;
            case PHOTO_CAMERA_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Kamera izni olmadan fotoğraf çekilemez.", Snackbar.LENGTH_LONG);
                }else {
                    startNewPhotoIntent();
                }
                break;
            case VIDEO_CAMERA_PERMISSION:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    parentActivity.showSnackbarMessage("Kamera izni olmadan video kaydedilemez.", Snackbar.LENGTH_LONG);
                }else {
                    startNewVideoIntent();
                }
                break;
            case LOCATION_PERMISSION_REQUEST:
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
                    intent.putExtra(Path.PATH_ID,path_id);
                    parentActivity.startService(intent);
                }
                break;
        }

    }

    public class ThumbnailGeneration implements Runnable{
        private String filePath;
        private String fileType;
        private String share_option;
        private Long time;
        private LocationManager manager;

        ThumbnailGeneration(String filePath, String fileType, String share_option){
            this.filePath = filePath;
            this.fileType = fileType;
            this.share_option = share_option;
            this.time = System.currentTimeMillis();
            manager = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE));
        }

        /**
         * Know that we have location permission
         * This function saves media file to database. If location not available on gps this thread
         * will wait until we have a location.
         */
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            Location lastknown = null;
            MediaFile mediaFile = null;
            while (lastknown == null){
                lastknown = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastknown != null) {
                    mediaFile = new MediaFile(fileType, filePath, lastknown.getLatitude(), lastknown.getLongitude(), lastknown.getAltitude(), trip.id, time, share_option);
                }
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mediaFiles.add(0,mediaFile);
            mediaFile.generateThumbNail(getActivity());
            helper.insertMediaFile(mediaFile);
            activityHandler.post(new Runnable() {
                @Override
                public void run() {
                    setUpPreview();
                }
            });
        }
    }
}
