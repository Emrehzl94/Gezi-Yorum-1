package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

/**
 * Shows trip info in Timeline fragment under viewpager.
 */

public class TripInfo extends TripSummary {
    private static int CHOOSE_COVER_REQUEST = 1;

    private int position;
    TimeLine parentFragment;
    private Boolean isLast;
    private ImageView cover;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        helper = new LocationDbOpenHelper(getContext());
        Long trip_id = bundle.getLong(Trip.TRIPID, -1);
        trip = helper.getTrip(trip_id);
        position = bundle.getInt("position",0);
        isLast = bundle.getBoolean("islast", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tripinfo_fragment, container, false);
        parentFragment = (TimeLine)getParentFragment();
        ImageButton next = view.findViewById(R.id.nextbutton);
        ImageButton prev = view.findViewById(R.id.prevbutton);
        if(position == 0){
            prev.setVisibility(View.INVISIBLE);
        }else {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.setPrevPage();
                }
            });
        }
        if(isLast){
            next.setVisibility(View.INVISIBLE);
        }else {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.setNextPage();
                }
            });
        }
        TextView header = view.findViewById(R.id.header);
        Trip trip_info = helper.getTrip(trip.id);
        String headertext = trip_info.name;
        header.setText(headertext);
        setUpView(view);
        parentFragment.setIsShared(getIsShared());

        cover = view.findViewById(R.id.cover);
        if(trip.cover_media_id != -1){
            cover.setImageBitmap(BitmapFactory.decodeFile(helper.getMediaFile(trip.cover_media_id).path));
        }
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(helper.getMediaFileCount(trip.id, MediaFile.PHOTO) > 0) {
                    Intent intent = new Intent(getContext(), MediaActivity.class);
                    intent.putExtra(Trip.TRIPID, trip.id);
                    startActivityForResult(intent, CHOOSE_COVER_REQUEST);
                }else {
                    Snackbar.make(view, getString(R.string.no_media), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }
    public long getTripId(){
        return trip.id;
    }
    public Boolean getIsShared(){ return trip.isShared;}

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CHOOSE_COVER_REQUEST) {
                trip = helper.getTrip(trip.id);
                cover.setImageBitmap(BitmapFactory.decodeFile(helper.getMediaFile(trip.cover_media_id).path));
            }
        }
    }
}
