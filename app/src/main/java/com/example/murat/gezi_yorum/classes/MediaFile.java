package com.example.murat.gezi_yorum.classes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Struct for carrying media files info
 */

public class MediaFile {
    public String type;
    public String path;
    public mLocation location;
    public long trip_id;
    public Bitmap thumbNail = null;

    public MediaFile(String type, String path,double latitude, double longitude, double altitude , long trip_id, long time){
        this.type = type;
        this.path = path;
        this.location = new mLocation(latitude,longitude,altitude,time);
        this.trip_id = trip_id;
    }
    public MediaFile(String type, String path,double latitude, double longitude, double altitude , long trip_id, long time,byte[] imageData){
        this(type,path,latitude,longitude,altitude,trip_id,time);
        thumbNail = BitmapFactory.decodeByteArray(imageData,0,imageData.length);
    }

    /**
     * Generates thumbnail for this file
     * @param activity current activity
     */
    public void generateThumbNail(Activity activity){
        if(thumbNail == null) {
            switch (type) {
                case Constants.PHOTO:
                    thumbNail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 100, 100);
                    break;
                case Constants.VIDEO:
                    thumbNail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    break;
                case Constants.SOUNDRECORD:
                    thumbNail = BitmapFactory.decodeResource(activity.getResources(), android.R.drawable.ic_btn_speak_now);
                    break;
                case Constants.NOTE:
                    thumbNail = BitmapFactory.decodeResource(activity.getResources(), android.R.drawable.ic_menu_agenda);
                    break;
            }
        }
    }

    /**
     * adds this file to map
     * @param map GoogleMap
     */

    public void addToMap(GoogleMap map){
        switch (type){
            case Constants.PHOTO:
                map.addMarker(new MarkerOptions().position(location.convertLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                );
                break;
            case Constants.VIDEO:
                map.addMarker(new MarkerOptions().position(location.convertLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                break;
            case Constants.SOUNDRECORD:
                map.addMarker(new MarkerOptions().position(location.convertLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                break;
            case Constants.NOTE:
                map.addMarker(new MarkerOptions().position(location.convertLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                break;
        }
    }
    /**
     * Returns thumbnail array created from media files
     * @param files MediaFile array
     * @return array
     */
    public static ArrayList<Bitmap> getThumbnailArray(ArrayList<MediaFile> files){
        ArrayList<Bitmap> return_array = new ArrayList<>();
        for (MediaFile file : files){
            return_array.add(file.thumbNail);
        }
        return return_array;
    }

    /**
     * Starts activity for this file
     * @param activity currentActivity
     */
    public void startActivityForView(Activity activity){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+path), getMimeForIntent());
        activity.startActivity(intent);
    }

    /**
     * returns Mime type
     * @return String
     */
    private String getMimeForIntent(){
        String mimeType = "";
        switch (type){
            case Constants.PHOTO:
                mimeType = "image/*";
                break;
            case Constants.VIDEO:
                mimeType = "video/*";
                break;
        }
        return mimeType;
    }
}
