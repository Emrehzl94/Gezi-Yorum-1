package com.example.murat.gezi_yorum.classes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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

    public MediaFile(String type, String path,double latitude, double longitude, double altitude , long trip_id, long time){
        this.type = type;
        this.path = path;
        this.location = new mLocation(latitude,longitude,altitude,time);
        this.trip_id = trip_id;
    }

    /**
     * Returns thumbnail of this file
     * @param activity
     * @return
     */
    public Bitmap getThumbnail(Activity activity){
        Bitmap return_value = null;
        switch (type) {
            case Constants.PHOTO:
                return_value = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path),100,100);
                break;
            case Constants.VIDEO:
                return_value = ThumbnailUtils.createVideoThumbnail(path,MediaStore.Video.Thumbnails.MICRO_KIND);
                break;
            case Constants.SOUNDRECORD:
                return_value = BitmapFactory.decodeResource(activity.getResources(),android.R.drawable.ic_btn_speak_now);
                break;
            case Constants.NOTE:
                return_value = BitmapFactory.decodeResource(activity.getResources(),android.R.drawable.ic_menu_agenda);
                break;
        }
        return return_value;
    }

    public void addToMap(GoogleMap map, Activity activity){
        switch (type){
            case Constants.PHOTO:
                map.addMarker(new MarkerOptions()
                        .position(location.convertLatLng())
                        .icon(BitmapDescriptorFactory.fromBitmap(getThumbnail(activity)))
                        .zIndex(1.0f)
                );
                break;
            case Constants.VIDEO:
                map.addMarker(new MarkerOptions().position(location.convertLatLng()));
                break;
            case Constants.SOUNDRECORD:
                map.addMarker(new MarkerOptions().position(location.convertLatLng()));
                break;
            case Constants.NOTE:
                map.addMarker(new MarkerOptions().position(location.convertLatLng()));
                break;
        }
    }
    /**
     * Returns thumbnail array created from media files
     * @param activity
     * @param files
     * @return
     */
    public static ArrayList<Bitmap> getThumbnailArray(Activity activity, ArrayList<MediaFile> files){
        ArrayList<Bitmap> return_array = new ArrayList<>();
        for (MediaFile file : files){
            return_array.add(file.getThumbnail(activity));
        }
        return return_array;
    }

    /**
     * Returns thumbnail array at limit 5 for preview
     * @param activity
     * @param files
     * @param type
     * @return
     */
    public static ArrayList<Bitmap> getThumbnailArrayForPreview(Activity activity, ArrayList<MediaFile> files, String type){
        int resourceId = 0;
        if(type.equals(Constants.PHOTO)){
            resourceId = android.R.drawable.ic_menu_camera;
        }else if(type.equals(Constants.VIDEO)){
            resourceId = android.R.drawable.presence_video_online;
        }else if(type.equals(Constants.SOUNDRECORD)){
            resourceId = android.R.drawable.ic_btn_speak_now;
        }else if(type.equals(Constants.NOTE)){
            resourceId = android.R.drawable.ic_menu_agenda;
        }
        ArrayList<Bitmap> return_array = getThumbnailArray(activity,files);

        return_array.add(BitmapFactory.decodeResource(activity.getResources(),resourceId));
        return return_array;
    }
}
