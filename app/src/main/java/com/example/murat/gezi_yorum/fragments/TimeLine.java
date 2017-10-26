package com.example.murat.gezi_yorum.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.TripPagerAdapter;
import com.example.murat.gezi_yorum.utils.CustomBottomSheetBehavior;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TimeLine extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private CustomBottomSheetBehavior behavior;
    private ViewPager viewPager;
    private TripPagerAdapter pagerAdapter;
    private ArrayList<Integer> trip_ids;
    private LocationDbOpenHelper helper;
    private final static int MAP_PERMISSION_REQUEST = 1;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.timeline));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        viewPager = view.findViewById(R.id.pager);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        params.height = metrics.heightPixels;
        params.width = metrics.widthPixels;
        viewPager.setLayoutParams(params);

        helper = new LocationDbOpenHelper(getContext());

        trip_ids = helper.getTripsInfo();
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
                PolylineOptions options = helper.getTripInfo(trip_ids.get(position));
                map.clear();
                if(!options.getPoints().isEmpty()) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(options.getPoints().get(0), 15.0f));
                    map.addPolyline(options);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = CustomBottomSheetBehavior.from(bottomsheet);
        behavior.setHideable(false);
        behavior.setState(CustomBottomSheetBehavior.STATE_COLLAPSED);
        behavior.setPeekHeight(300);

        behavior.setBottomSheetCallback(new CustomBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @CustomBottomSheetBehavior.State int newState) {
                // each time the bottomsheet changes position, animate the camera to keep the pin in view
                // normally this would be a little more complex (getting the pin location and such),
                // but for the purpose of an example this is enough to show how to stay centered on a pin
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        Button rightButton = (Button) view.findViewById(R.id.right_button);
        Button leftButton = (Button) view.findViewById(R.id.left_button);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = viewPager.getCurrentItem();
                if(page < pagerAdapter.getCount()){
                    viewPager.setCurrentItem(page+1);
                }
            }
        });
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = viewPager.getCurrentItem();
                if(page > 0){
                    viewPager.setCurrentItem(page-1);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timeline_fragment, container, false);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        behavior.anchorMap(map);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MAP_PERMISSION_REQUEST);
            return;
        }
        map.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
        if(lastKnownLocation != null) {
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng,13.0f));
        }

        PolylineOptions options = helper.getTripInfo(trip_ids.get(trip_ids.size()-1));
        map.addPolyline(options);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                behavior.setState(CustomBottomSheetBehavior.STATE_COLLAPSED);
                behavior.setHideable(false);
                map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(behavior.getState() == CustomBottomSheetBehavior.STATE_HIDDEN){
                    behavior.setHideable(false);
                    behavior.setState(CustomBottomSheetBehavior.STATE_COLLAPSED);
                }else {
                    behavior.setHideable(true);
                    behavior.setState(CustomBottomSheetBehavior.STATE_HIDDEN);
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
}
