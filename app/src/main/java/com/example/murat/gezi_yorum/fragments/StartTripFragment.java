package com.example.murat.gezi_yorum.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.ContinuingTrip;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;

/**
 * Trip configuration before start
 */

public class StartTripFragment extends Fragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_trip_fragment, container, false);
        FloatingActionButton fab = view.findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity parentActivity = (MainActivity) getActivity();
                parentActivity.startTrip();
            }
        });
        fab.setBackgroundColor(Color.GREEN);
        return view;
    }
}
