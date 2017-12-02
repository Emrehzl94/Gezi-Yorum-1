package com.example.murat.gezi_yorum.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;

/**
 * Trip configuration before start
 */

public class StartTripFragment extends Fragment{

    private EditText trip_name_edit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_trip_fragment, container, false);

        trip_name_edit = view.findViewById(R.id.trip_name);

        FloatingActionButton fab = view.findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContinuingTrip continuingTrip = new ContinuingTrip();
                Bundle extras = new Bundle();
                extras.putString(Constants.MESSAGE,Constants.STARTNEWTRIP);
                extras.putString(Constants.TRIPNAME, trip_name_edit.getText().toString());
                continuingTrip.setArguments(extras);
                MainActivity parentActivity = (MainActivity) getActivity();
                parentActivity.changeFragment(continuingTrip);
            }
        });
        fab.setBackgroundColor(Color.GREEN);
        return view;
    }
}
