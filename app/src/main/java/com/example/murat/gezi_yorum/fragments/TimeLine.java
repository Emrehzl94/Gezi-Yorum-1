package com.example.murat.gezi_yorum.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.TripPagerAdapter;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TimeLine extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private BottomSheetBehavior behavior;
    private ViewPager viewPager;
    private TripPagerAdapter pagerAdapter;
    private ArrayList<Integer> trip_ids;
    private LocationDbOpenHelper helper;
    private final static int MAP_PERMISSION_REQUEST = 1;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.timeline));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        viewPager = view.findViewById(R.id.pager);
        helper = new LocationDbOpenHelper(getContext());
        trip_ids = helper.getTripsIDs();
        pagerAdapter = new TripPagerAdapter(getChildFragmentManager());
        pagerAdapter.setCount(trip_ids.size());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(trip_ids.size());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                map.clear();
                ArrayList<LatLng> points = helper.getTripPath(trip_ids.get(position));

                PolylineOptions options = new PolylineOptions();
                if (!points.isEmpty()) {
                    options.color(Color.RED);
                    options.width(15);
                    options.visible(true);
                    options.addAll(points);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng point : points){
                        builder.include(point);
                    }
                    int routePadding = 100;

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),routePadding));
                    map.addPolyline(options);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MAP_PERMISSION_REQUEST);
            return;
        }
        map = googleMap;
        map.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null) {
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 13.0f));
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
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
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = false;
        switch (requestCode){
            case MAP_PERMISSION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    granted = true;
                }
                break;
        }
        MainActivity mainActivity = ((MainActivity) getActivity());
        if(granted){
            mainActivity.changeFragment(new TimeLine());
        }else {
            mainActivity.showSnackbarMessage("Bu işlev konum izni olmadan çalışmaz",Snackbar.LENGTH_LONG);
        }
    }

    public void setNextPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
    }
    public void setPrevPage(){
        viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }
    public int getIdAtPosition(int position){
        return trip_ids.get(position);
    }

}
