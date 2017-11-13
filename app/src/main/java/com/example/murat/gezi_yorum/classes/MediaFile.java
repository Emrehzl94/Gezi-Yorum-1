package com.example.murat.gezi_yorum.classes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Struct for carring media files info
 */

public class MediaFile {
    public String type;
    public String path;
    public mLocation location;
    public long trip_id;
    public MediaFile(String type, String path,long longitude, long latitude, long altitude , long trip_id, long date){
        this.type = type;
        this.path = path;
        this.location = new mLocation(longitude,latitude,altitude,date);
        this.trip_id = trip_id;
    }
    public Bitmap getBitmap(Activity activity){
        Bitmap return_value = null;
        switch (type) {
            case Constants.PHOTO:
                try {
                   return_value = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), Uri.fromFile(new File(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    public static ArrayList<Bitmap> getBitmapArray(Activity activity, ArrayList<MediaFile> files){
        ArrayList<Bitmap> return_array = new ArrayList<>();
        for (MediaFile file : files){
            return_array.add(file.getBitmap(activity));
        }
        return return_array;
    }
}
