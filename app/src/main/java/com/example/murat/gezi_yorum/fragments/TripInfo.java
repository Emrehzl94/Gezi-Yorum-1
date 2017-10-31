package com.example.murat.gezi_yorum.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

import java.util.HashMap;

/**
 * Created by murat on 24.10.2017.
 */

public class TripInfo extends Fragment {
    private String title;
    private int page;
    private ImageButton next, prev;
    TimeLine parentActivity;
    LocationDbOpenHelper helper;
    // newInstance constructor for creating fragment with arguments
    public static TripInfo newInstance(int page, String title) {
        TripInfo fragmentFirst = new TripInfo();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tripinfo_fragment, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.trytext);
        tvLabel.setText(page + " -- " + title);
        parentActivity = (TimeLine)getParentFragment();
        next = view.findViewById(R.id.nextbutton);
        prev = view.findViewById(R.id.prevbutton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setNextPage();
            }
        });
        if(page == 0){
            prev.setVisibility(View.INVISIBLE);
        }else {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.setPrevPage();
                }
            });
        }
        TextView header = view.findViewById(R.id.header);
        helper = new LocationDbOpenHelper(getContext());
        HashMap<String,String> trip_info = helper.getTripsInfo(parentActivity.getIdAtPosition(page));
        String headertext = trip_info.get("startdate") + "\n" + trip_info.get("finishdate");
        header.setText(headertext);

        return view;
    }
}
