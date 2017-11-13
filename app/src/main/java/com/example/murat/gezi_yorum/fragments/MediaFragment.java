package com.example.murat.gezi_yorum.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.murat.gezi_yorum.NoteTake;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.RecordAudio;
import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Fragment for watch media files
 */

public class MediaFragment extends Fragment {

    private int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_VIDEO_CAPTURE = 2;
    private int REQUEST_SOUND_RECORD= 3;
    private int REQUEST_TAKE_NOTE= 4;

    private LocationDbOpenHelper helper;
    private long trip_id;
    private GridView mediaView;
    private ArrayList<Bitmap> media;
    private Uri lastOutputMedia;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        View view = inflater.inflate(R.layout.media_fragment, container, false);
        mediaView = view.findViewById(R.id.media_view);
        media = new ArrayList<>();

        Bundle arguments = getArguments();
        String action = arguments.getString(Constants.ACTION);

        if(action != null){
            switch (action) {
                case Constants.PHOTO:
                    startNewPhotoIntent();
                    break;
                case Constants.VIDEO:
                    startNewVideoIntent();
                    break;
                case Constants.SOUNDRECORD:
                    startNewAudioIntent();
                    break;
                case Constants.NOTE:
                    startNewNoteIntent();
                    break;
            }
        }
        trip_id = getActivity().getPreferences(Context.MODE_PRIVATE).getLong(Constants.TRIPID, -1);
        helper = new LocationDbOpenHelper(getContext());
        ArrayList<MediaFile> files = helper.getMedias(trip_id,Constants.PHOTO);
        for(MediaFile file : files){
            switch (file.type) {
                case Constants.PHOTO:
                    try {
                        media.add(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(new File(file.path))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.VIDEO:
                    media.add(ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Video.Thumbnails.MICRO_KIND));
                    break;
                case Constants.SOUNDRECORD:
                    media.add(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_btn_speak_now));
                    break;
            }
        }
        mediaView.setAdapter(new MediaGridViewAdapter(getContext(),media));
        return view;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            String type = "";
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                try {
                    media.add(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),lastOutputMedia));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                type = Constants.PHOTO;
                startNewPhotoIntent();
            }else if(requestCode == REQUEST_VIDEO_CAPTURE){
                lastOutputMedia = data.getData();
                media.add(ThumbnailUtils.createVideoThumbnail(lastOutputMedia.getPath(),MediaStore.Video.Thumbnails.MICRO_KIND));
                type = Constants.VIDEO;
                startNewVideoIntent();
            }else if(requestCode == REQUEST_SOUND_RECORD){
                type = Constants.SOUNDRECORD;
                media.add(BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_btn_speak_now));
            }else if(requestCode == REQUEST_TAKE_NOTE){
                type = Constants.NOTE;
            }
            @SuppressLint("MissingPermission") Location lastknown = ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            helper.insertMediaFile(new MediaFile(type,lastOutputMedia.getPath(),(long) lastknown.getLongitude(),(long)lastknown.getLatitude(),(long)lastknown.getAltitude(),trip_id,System.currentTimeMillis()));
        }
        else {
            mediaView.setAdapter(new MediaGridViewAdapter(getContext(),media));
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
        }
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)),subdir);
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        File mediaFile = new File(storageDir.getPath() + File.separator + System.currentTimeMillis()+"."+extension);
        return Uri.fromFile(mediaFile);
    }
}
