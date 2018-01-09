package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.TripPagerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class TimeLine extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private BottomSheetBehavior behavior;
    private ViewPager viewPager;

    private TripPagerAdapter pagerAdapter;
    private Integer currentPosition;

    private Boolean loadImporteds = false;
    private User user;
    private LocationDbOpenHelper helper;

    private View nothing;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomsheet = view.findViewById(R.id.bottomsheet);

        nothing = view.findViewById(R.id.nothing);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setHideable(false);
        behavior.setPeekHeight(300);

        viewPager = view.findViewById(R.id.pager);
        helper = new LocationDbOpenHelper(getContext());
        user = new User(getContext().getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE));
        Bundle args = getArguments();
        if(args != null){
            loadImporteds = args.getBoolean("isImported", false);
        }

        if(args != null && args.getBoolean("jump", false)) {
            currentPosition = args.getInt("position");
            getActivity().setTitle(getString(R.string.trip));
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(map != null){
                    lastClickedMarker = null;
                    currentPosition = position;
                    TripInfo currentFragment = pagerAdapter.getFragment(currentPosition);
                    if(currentFragment!=null)
                        currentFragment.requestToDrawPathOnMap(map,false, null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        loadAdapter();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(TimeLine.this);
    }
    public void loadAdapter(){
        ArrayList<Long> trip_ids;
        if(loadImporteds){
            trip_ids = helper.getTripsIDsForTimeLine(true, user.username);
            getActivity().setTitle(getString(R.string.downloads));
        }else {
            trip_ids = helper.getTripsIDsForTimeLine(false, user.username);
            getActivity().setTitle(getString(R.string.timeline));
        }
        if(trip_ids.size() == 0){
            nothing.setVisibility(View.VISIBLE);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            currentPosition = 0;
        }
        if(currentPosition == null){
            currentPosition = trip_ids.size() - 1;
        }else if(currentPosition == trip_ids.size() && currentPosition != 0){
            currentPosition -= 1;
        }
        if(pagerAdapter == null)
            pagerAdapter = new TripPagerAdapter(getChildFragmentManager(), trip_ids);
        else {
            if(pagerAdapter.getCount() != trip_ids.size())
                 pagerAdapter.setDataset(trip_ids);
        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentPosition);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timeline_fragment, container, false);
    }

    Marker lastClickedMarker;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getContext(), MediaActivity.class);
                intent.putExtra("fileIds",marker.getSnippet());
                getActivity().startActivity(intent);
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(behavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    behavior.setHideable(false);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else {
                    behavior.setHideable(true);
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        if(pagerAdapter.getCount()>0) {
            TripInfo fragment = pagerAdapter.getFragment(currentPosition);
            fragment.requestToDrawPathOnMap(map, true, null);
        }
    }

    public void setNextPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
    }
    public void setPrevPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }
    public GoogleMap getMap(){
        return map;
    }
}
