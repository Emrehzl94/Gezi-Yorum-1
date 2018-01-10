package com.example.murat.gezi_yorum.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.murat.gezi_yorum.Entity.MediaFile;

import java.util.ArrayList;

/**
 * List photos
 */


public class MediaGridViewAdapter extends ArrayAdapter<Bitmap> {
    private Activity activity;
    private ArrayList<MediaFile> media;

    public MediaGridViewAdapter(Activity activity, ArrayList<MediaFile> media) {
        super(activity.getApplicationContext(), -1,MediaFile.getThumbnailArray(media));
        this.activity = activity;
        this.media = media;
    }

    @Override
    public int getCount() {
        return media.size() > 0 ? media.size() : 1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(2,2,2,2);
        imageView.setAdjustViewBounds(true);
        if(media.size()>0) {
            imageView.setImageBitmap(media.get(position).thumbNail);
        }else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        return imageView;
    }
}
