package com.example.murat.gezi_yorum.Entity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.fragments.PhotoFragment;
import com.example.murat.gezi_yorum.fragments.VideoFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Struct for carrying media files info
 */

public class MediaFile {
    public Long id;
    public String type;
    public String path;
    public mLocation location;
    public long trip_id;
    public Bitmap thumbNail = null;
    public String share_option;
    public String about_note;

    /**
     *This constructor used while adding new media
     */
    public MediaFile(String type, String path,double latitude, double longitude, double altitude , long trip_id, long time, String share_option){
        this.type = type;
        this.path = path;
        this.location = new mLocation(latitude,longitude,altitude,time);
        this.trip_id = trip_id;
        this.share_option = share_option;
        this.about_note = "";
    }

    /**
     * This constructor used by DB
     */
    public MediaFile(Long id,String type, String path,double latitude, double longitude, double altitude , long trip_id, long time,byte[] imageData, String share_option, String about_note){
        this(type, path, latitude, longitude, altitude, trip_id, time, share_option);
        thumbNail = BitmapFactory.decodeByteArray(imageData,0,imageData.length);
        this.id = id;
        this.about_note = about_note;
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
            }
        }
    }

    /**
     * adds this file to map
     * @param map GoogleMap
     */

    public Marker addToMap(GoogleMap map){
        return map.addMarker(new MarkerOptions().position(location.convertLatLng())
                .icon(BitmapDescriptorFactory.defaultMarker(getColorForMap()))
                .title(String.valueOf(id))
                .snippet(String.valueOf(id))
        );
    }

    public float getColorForMap(){
        float color = 0;
        switch (type){
            case Constants.PHOTO:
                color = BitmapDescriptorFactory.HUE_RED;
                break;
            case Constants.VIDEO:
                color = BitmapDescriptorFactory.HUE_BLUE;
                break;
            case Constants.SOUNDRECORD:
                color = BitmapDescriptorFactory.HUE_GREEN;
                break;
        }
        return color;
    }


    public static String getSubdir(String type){
        String subdir = "";
        switch (type) {
            case Constants.PHOTO:
                subdir = "Photos";
                break;
            case Constants.VIDEO:
                subdir = "Videos";
                break;
            case Constants.SOUNDRECORD:
                subdir = "Audio";
                break;
        }
        return subdir;
    }
    public static String getExtension(String type){
        String extension = "";
        switch (type) {
            case Constants.PHOTO:
                extension = "jpg";
                break;
            case Constants.VIDEO:
                extension = "mp4";
                break;
            case Constants.SOUNDRECORD:
                extension = "mp3";
                break;
        }
        return extension;
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
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra("fileIds",new long[]{id});
        activity.startActivity(intent);
    }

    /**
     * Returns compatiple viewer for media type
     * @return Fragment
     */
    public Fragment getViewer(){
        Fragment fragment = null;
        switch (type){
            case Constants.PHOTO:
                fragment = new PhotoFragment();
                break;
            case Constants.VIDEO:
                fragment = new VideoFragment();
                break;
            case Constants.SOUNDRECORD:
                fragment = new VideoFragment();
                break;
        }
        return fragment;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path",path);
            jsonObject.put("type",type);
            jsonObject.put("longitude",location.getLongitude());
            jsonObject.put("latitude",location.getLatitude());
            jsonObject.put("altitude",location.getAltitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public byte[] getByteArray(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbNail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

}
