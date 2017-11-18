package com.example.murat.gezi_yorum.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.TripPagerAdapter;
import com.example.murat.gezi_yorum.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class TimeLine extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private BottomSheetBehavior behavior;
    private ViewPager viewPager;
    private TripPagerAdapter pagerAdapter;
    private LocationDbOpenHelper helper;

    private int currentPosition;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.timeline));
        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton sharetrip = view.findViewById(R.id.share_trip);
        sharetrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pagerAdapter.getFragment(currentPosition).shareTrip();
            }
        });
        helper = new LocationDbOpenHelper(getContext());
        viewPager = view.findViewById(R.id.pager);
        LocationDbOpenHelper helper = new LocationDbOpenHelper(getContext());
        ArrayList<Integer> trip_ids = helper.getTripsIDs();
        pagerAdapter = new TripPagerAdapter(getChildFragmentManager(), trip_ids);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(trip_ids.size());
        currentPosition = trip_ids.size();
        if(trip_ids.size()==0){
            sharetrip.setVisibility(View.INVISIBLE);
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
                    pagerAdapter.getFragment(position).drawPathOnMap(map,false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setState(BottomSheetBehavior.STATE_DRAGGING);
        behavior.setHideable(false);
        behavior.setPeekHeight(300);
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
                if(marker.getZIndex() != 1){
                    if(lastClickedMarker!=null){
                        lastClickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(helper.getMediaFile(Long.parseLong(lastClickedMarker.getTitle())).getColorForMap()));
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(helper.getMediaFile(Long.parseLong(marker.getTitle())).thumbNail));
                    marker.setZIndex(1);
                    lastClickedMarker = marker;
                }else {
                    helper.getMediaFile(Long.parseLong(marker.getTitle())).startActivityForView(getActivity());
                    marker.setZIndex(0);
                    lastClickedMarker = null;
                }
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behavior.setHideable(false);
                map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
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
                if(lastClickedMarker!=null){
                    lastClickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(helper.getMediaFile(Long.parseLong(lastClickedMarker.getTitle())).getColorForMap()));
                    lastClickedMarker.setZIndex(0);
                }
            }
        });
        if(pagerAdapter.getCount()>0) {
            pagerAdapter.getFragment(pagerAdapter.getCount()-1).drawPathOnMap(map,true);
        }
    }

    public void setNextPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
    }
    public void setPrevPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }

}
