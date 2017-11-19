package com.example.murat.gezi_yorum.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.classes.LocationCSVHandler;
import com.example.murat.gezi_yorum.classes.ZipFileUploader;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;

/**
 * Shows trip info in Timeline fragment under viewpager.
 */

public class TripInfo extends TripSummary {
    private int position;
    private ImageButton next, prev;
    TimeLine parentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        trip_id = bundle.getInt("trip_id", -1);
        position = bundle.getInt("position",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tripinfo_fragment, container, false);
        parentFragment = (TimeLine)getParentFragment();
        next = view.findViewById(R.id.nextbutton);
        prev = view.findViewById(R.id.prevbutton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.setNextPage();
            }
        });
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
        TextView header = view.findViewById(R.id.header);
        helper = new LocationDbOpenHelper(getContext());
        HashMap<String,String> trip_info = helper.getTripInfo(trip_id);
        String headertext = trip_info.get("startdate") + "\n" + trip_info.get("finishdate");
        header.setText(headertext);
        setUpView(view);
        return view;
    }
    public void drawPathOnMap(GoogleMap map ,boolean move) {
        if (map != null) {
            this.map = map;
            map.clear();
            points = new LocationCSVHandler(trip_id,getContext()).getLocations();

            PolylineOptions options = new PolylineOptions();
            if (!points.isEmpty()) {
                options.color(Color.RED);
                options.width(15);
                options.visible(true);
                options.addAll(points);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : points) {
                    builder.include(point);
                }
                int routePadding = 200;
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), routePadding);
                if (move) {
                    map.moveCamera(update);
                } else {
                    map.animateCamera(update);
                }
                addedPolyLine = map.addPolyline(options);
                addMarkersToMap();
            }
        }
    }
    public long getTripId(){
        return trip_id;
    }
}
