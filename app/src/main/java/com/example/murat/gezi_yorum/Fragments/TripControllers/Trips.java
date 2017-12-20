package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.TripsAdapter;


/**
 * Lists Trips
 */

public class Trips extends Fragment {
    TripsAdapter listViewAdapter;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.trips));
        ListView listView = view.findViewById(R.id.trip_list);
        listViewAdapter = new TripsAdapter(getContext(), false);
        listView.setAdapter(listViewAdapter);
        if(listViewAdapter.getCount() == 0){
            view.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = new TimeLine();
                Bundle extras = new Bundle();
                extras.putBoolean("jump",true);
                extras.putInt("position", i);
                fragment.setArguments(extras);
                ((MainActivity)getActivity()).changeFragment(fragment);
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.trips_fragment,container,false);
    }
}
