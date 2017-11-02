package com.example.murat.gezi_yorum.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * List photos
 */

public class PhotoListViewAdapter extends ArrayAdapter<Bitmap> {
    private Context context;
    private ArrayList<Bitmap> values;

    public PhotoListViewAdapter(Context context, ArrayList<Bitmap> values) {
        super(context, -1,values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(2,2,2,2);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(values.get(position));
        return imageView;
    }
}
