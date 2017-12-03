package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

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
        trip_id = bundle.getLong(Constants.TRIPID, -1);
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
        Trip trip_info = helper.getTrip(trip_id);
        String headertext = trip_info.name;
        header.setText(headertext);
        setUpView(view);
        return view;
    }
    public long getTripId(){
        return trip_id;
    }
}
