package com.example.murat.gezi_yorum.Fragments.TripControllers;

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

import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.TripPagerAdapter;
import com.example.murat.gezi_yorum.ZipFileUploader;
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
    private int currentPosition;
    private Boolean currentIsShared = false;
    private FloatingActionButton shareTrip;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setHideable(false);
        behavior.setPeekHeight(300);

        shareTrip = view.findViewById(R.id.share_trip);
        shareTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!currentIsShared) {
                    Snackbar.make(view, getString(R.string.trip_sharing), Snackbar.LENGTH_LONG).show();
                    long trip_id = pagerAdapter.getFragment(currentPosition).getTripId();
                    Intent intent = new Intent(getContext(), ZipFileUploader.class);
                    intent.putExtra(Trip.TRIPID, trip_id);
                    getActivity().startService(intent);
                }else {
                    Snackbar.make(view, getString(R.string.shared_before), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        viewPager = view.findViewById(R.id.pager);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(TimeLine.this);
        LocationDbOpenHelper helper = new LocationDbOpenHelper(getContext());
        ArrayList<Long> trip_ids;
        Bundle args = getArguments();
        if(args != null && args.getBoolean("isImported", false)){
            trip_ids = helper.getTripsIDsForTimeLine(true);
            getActivity().setTitle(getString(R.string.downloads));
            shareTrip.setVisibility(View.INVISIBLE);
        }else {
            trip_ids = helper.getTripsIDsForTimeLine(false);
            getActivity().setTitle(getString(R.string.timeline));
        }
        if(trip_ids.size() == 0){
            shareTrip.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else {
            behavior.setState(BottomSheetBehavior.STATE_DRAGGING);
        }

        if(args != null && args.getBoolean("jump", false)) {
            currentPosition = args.getInt("position");
            getActivity().setTitle(getString(R.string.trip));
        }else {
            currentPosition = trip_ids.size() - 1;
        }
        pagerAdapter = new TripPagerAdapter(getChildFragmentManager(), trip_ids);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pagerAdapter.getCount());
        if(pagerAdapter.getCount()==0){
            shareTrip.setVisibility(View.INVISIBLE);
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
                    currentFragment.requestToDrawPathOnMap(map,false, null);
                    setIsShared(currentFragment.getIsShared());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
            pagerAdapter.getFragment(currentPosition).requestToDrawPathOnMap(map,true, null);
        }
    }

    public void setNextPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
    }
    public void setPrevPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }
    public void setIsShared(Boolean isShared){
        currentIsShared = isShared;
        if(isShared){
            shareTrip.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }else {
            shareTrip.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
    }
}
