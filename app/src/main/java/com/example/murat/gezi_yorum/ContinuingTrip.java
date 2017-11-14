package com.example.murat.gezi_yorum;

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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class ContinuingTrip extends Fragment implements OnMapReadyCallback, LocationSource, LocationListener {

    private int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_VIDEO_CAPTURE = 2;
    private int REQUEST_SOUND_RECORD= 3;
    private int REQUEST_TAKE_NOTE= 4;

    private Uri lastOutputMedia;

    private BottomSheetBehavior behavior;
    private Button pause_continue;
    private View.OnClickListener pause, continue_listener;
    private MainActivity parentActivity;
    private long trip_id;
    private OnLocationChangedListener listener;
    private GoogleMap map;
    private LocationManager locationManager;
    private Polyline addedPolyline;
    private ArrayList<LatLng> points;
    private LocationDbOpenHelper helper;
    private ArrayList<MediaFile> photos;
    private ArrayList<MediaFile> videos;
    private ArrayList<MediaFile> sounds;
    private ArrayList<MediaFile> notes;
    public void setTrip_id(long trip_id) {
        this.trip_id = trip_id;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
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

        View view = inflater.inflate(R.layout.continuing_trip_fragment, container, false);

        helper = new LocationDbOpenHelper(getContext());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getActivity().setTitle("Devam eden");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        GridView photo_preview = view.findViewById(R.id.photo_preview_grid);
        GridView video_preview = view.findViewById(R.id.video_preview_grid);
        GridView sound_preview = view.findViewById(R.id.sound_preview_grid);
        GridView note_peview = view.findViewById(R.id.note_preview_grid);

        /*
          Getting preview for media files
         */
        photos = helper.getMediaFilesForPreview(trip_id, Constants.PHOTO);
        videos = helper.getMediaFilesForPreview(trip_id, Constants.VIDEO);
        sounds = helper.getMediaFilesForPreview(trip_id, Constants.SOUNDRECORD);
        notes = helper.getMediaFilesForPreview(trip_id, Constants.NOTE);
        photo_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArrayForPreview(getActivity(),photos,Constants.PHOTO)));
        photo_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position==photos.size()){
                    startNewPhotoIntent();
                }else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://"+photos.get(position).path), "image/*");
                    startActivity(intent);
                }
            }
        });
        video_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArrayForPreview(getActivity(),videos,Constants.VIDEO)));
        video_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position==videos.size()){
                    startNewVideoIntent();
                }else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://"+videos.get(position).path), "video/*");
                    startActivity(intent);
                }
            }
        });
        sound_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArrayForPreview(getActivity(), sounds,Constants.SOUNDRECORD)));
        sound_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position==sounds.size()){
                    startNewAudioIntent();
                }else {
                }
            }
        });
        note_peview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArrayForPreview(getActivity(),notes,Constants.NOTE)));
        note_peview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position==notes.size()){
                    startNewNoteIntent();
                }else {
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        if(map!=null){
            getInfoFromDb();
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
        for (MediaFile photo: photos){
            photo.addToMap(map,parentActivity);
        }
        for (MediaFile video: videos){
            video.addToMap(map,parentActivity);
        }
        for (MediaFile sound: sounds){
            sound.addToMap(map,parentActivity);
        }
        for (MediaFile note: notes){
            note.addToMap(map,parentActivity);
        }

        getInfoFromDb();

    }

    /**
     * Drawing trip path on the map
     */
    public void getInfoFromDb(){
        points = new LocationDbOpenHelper(getContext()).getTripPath(trip_id);

        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(15);
        options.visible(true);
        options.addAll(points);
        if(!points.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : points) {
                builder.include(point);
            }
            int routePadding = 100;
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), routePadding));
        }else {
            @SuppressLint("MissingPermission") Location lastknown = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastknown.getLatitude(),lastknown.getLongitude())));
        }
        addedPolyline = map.addPolyline(options);
    }

    /**
     * Adding new point to polyline
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        if(listener != null){
            listener.onLocationChanged(location);
            if(addedPolyline != null && points != null){
                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
                addedPolyline.setPoints(points);
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


    public void startNewPhotoIntent() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        lastOutputMedia = getOutpuMediaFile(Constants.PHOTO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }
    public void startNewVideoIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        lastOutputMedia = getOutpuMediaFile(Constants.VIDEO);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,lastOutputMedia);
        startActivityForResult(cameraIntent, REQUEST_VIDEO_CAPTURE);
    }
    public void startNewAudioIntent() {
        Intent audioIntent = new Intent(getContext(), RecordAudio.class);
        audioIntent.putExtra(MediaStore.EXTRA_OUTPUT,getOutpuMediaFile(Constants.SOUNDRECORD));
        startActivityForResult(audioIntent, REQUEST_SOUND_RECORD);
    }
    public void startNewNoteIntent(){
        Intent noteIntent = new Intent(getContext(), NoteTake   .class);
        startActivityForResult(noteIntent, REQUEST_TAKE_NOTE);
    }
    public void startMediaActivity(String actionType){
        Intent intent = new Intent(getContext(),MediaActivity.class);
        intent.putExtra(Constants.ACTION,actionType);
        intent.putExtra(Constants.TRIPID,trip_id);
        startActivity(intent);
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
                startNewVideoIntent();
            }else if(requestCode == REQUEST_SOUND_RECORD){
                outputMedia = lastOutputMedia;
                type = Constants.SOUNDRECORD;
            }else if(requestCode == REQUEST_TAKE_NOTE){
                outputMedia = lastOutputMedia;
                type = Constants.NOTE;
            }
            @SuppressLint("MissingPermission") Location lastknown = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            helper.insertMediaFile(new MediaFile(type,outputMedia.getPath(),lastknown.getLatitude(),lastknown.getLongitude(), lastknown.getAltitude(),trip_id,System.currentTimeMillis()));
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
        File mediaFile = new File(storageDir.getPath() + File.separator + System.currentTimeMillis()+"."+extension);
        return Uri.fromFile(mediaFile);
    }
}
