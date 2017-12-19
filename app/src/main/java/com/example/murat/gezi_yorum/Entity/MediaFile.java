package com.example.murat.gezi_yorum.Entity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.example.murat.gezi_yorum.Fragments.MediaFragments.PhotoFragment;
import com.example.murat.gezi_yorum.Fragments.MediaFragments.VideoFragment;
import com.example.murat.gezi_yorum.MediaActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    // Media types
    public static final String PHOTO = "photo";
    public static final String VIDEO = "video";
    public static final String SOUNDRECORD = "record";

    //Share options
    public static final String SHARE_OPTION = "share_option"; // Media share option chosen from user
    public static final String EVERYBODY = "everybody";
    public static final String MY_FRIENDS = "only_friends";
    public static final String ONLY_ME = "only_me";
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

    public MediaFile(JSONObject object, String path, Long trip_id){
        try {
            this.type = object.getString("type");
            this.path = path;
            this.location = new mLocation(object.getDouble("latitude"), object.getDouble("longitude"),
                    object.getDouble("altitude"), System.currentTimeMillis());
            this.trip_id = trip_id;
            this.share_option = EVERYBODY;
            this.about_note = object.getString("note");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates thumbnail for this file
     * @param context current activity
     */
    public void generateThumbNail(Context context){
        if(thumbNail == null) {
            switch (type) {
                case PHOTO:
                    thumbNail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 100, 100);
                    break;
                case VIDEO:
                    thumbNail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    break;
                case SOUNDRECORD:
                    thumbNail = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_btn_speak_now);
                    break;
            }
        }
    }

    /**
     * adds this file to map
     * @param map GoogleMap
     */

    public void addToMap(GoogleMap map, Boolean isImported){
        float color = isImported ? BitmapDescriptorFactory.HUE_BLUE : getColorForMap();
        map.addMarker(new MarkerOptions().position(location.convertLatLng())
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .snippet(String.valueOf(id))
        );
    }

    private float getColorForMap(){
        float color = 0;
        switch (type){
            case PHOTO:
                color = BitmapDescriptorFactory.HUE_RED;
                break;
            case VIDEO:
                color = BitmapDescriptorFactory.HUE_BLUE;
                break;
            case SOUNDRECORD:
                color = BitmapDescriptorFactory.HUE_GREEN;
                break;
        }
        return color;
    }


    public static String getSubdir(String type){
        String subdir = "";
        switch (type) {
            case PHOTO:
                subdir = "Photos";
                break;
            case VIDEO:
                subdir = "Videos";
                break;
            case SOUNDRECORD:
                subdir = "Audio";
                break;
        }
        return subdir;
    }
    public static String getExtension(String type){
        String extension = "";
        switch (type) {
            case PHOTO:
                extension = "jpg";
                break;
            case VIDEO:
                extension = "mp4";
                break;
            case SOUNDRECORD:
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
        intent.putExtra("fileIds",String.valueOf(id));
        activity.startActivity(intent);
    }

    /**
     * Returns compatible viewer for media type
     * @return Fragment
     */
    public Fragment getViewer(){
        Fragment fragment = null;
        switch (type){
            case PHOTO:
                fragment = new PhotoFragment();
                break;
            case VIDEO:
                fragment = new VideoFragment();
                break;
            case SOUNDRECORD:
                fragment = new VideoFragment();
                break;
        }
        return fragment;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path", new File(path).getName());
            jsonObject.put("type", type);
            jsonObject.put("longitude", location.getLongitude());
            jsonObject.put("latitude", location.getLatitude());
            jsonObject.put("altitude", location.getAltitude());
            jsonObject.put("share_option", share_option);
            jsonObject.put("note", about_note);
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
