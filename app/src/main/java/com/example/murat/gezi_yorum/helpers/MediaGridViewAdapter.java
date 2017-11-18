package com.example.murat.gezi_yorum.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.murat.gezi_yorum.classes.MediaFile;

import java.util.ArrayList;

/**
 * List photos
 */


public class MediaGridViewAdapter extends ArrayAdapter<Bitmap> {
    private Context context;
    private ArrayList<MediaFile> media;

    public MediaGridViewAdapter(Context context, ArrayList<MediaFile> media) {
        super(context, -1,MediaFile.getThumbnailArray(media));
        this.context = context;
        this.media = media;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(2,2,2,2);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(media.get(position).thumbNail);
        return imageView;
    }
}
