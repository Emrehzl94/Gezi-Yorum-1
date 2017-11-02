package com.example.murat.gezi_yorum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;


public class ContinuingTrip extends Fragment implements OnMapReadyCallback, LocationSource, LocationListener {

    private BottomSheetBehavior behavior;
    private Button pause_continue;
    private View.OnClickListener pause, continue_listener;
    private MainActivity parentActivity;
    private long startDate;
    private OnLocationChangedListener listener;
    private GoogleMap map;
    private LocationManager locationManager;
    private Polyline addedPolyline;
    private ArrayList<LatLng> points;

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.continuing_trip_fragment, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getActivity().setTitle("Devam eden");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pause_continue = view.findViewById(R.id.pause_continue);
        Button stop = view.findViewById(R.id.stop);
        parentActivity = (MainActivity) getActivity();
        pause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.stopRecording();
                pause_continue.setOnClickListener(continue_listener);
                pause_continue.setText("Contiune");
                pause_continue.setBackgroundColor(Color.GREEN);
            }
        };
        continue_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.startRecording();
                pause_continue.setOnClickListener(pause);
                pause_continue.setText("Pause");
                pause_continue.setBackgroundColor(Color.YELLOW);
            }
        };
        if (LocationSaveService.instance == null) {
            pause_continue.setOnClickListener(continue_listener);
            pause_continue.setText("Contiune");
            pause_continue.setBackgroundColor(Color.GREEN);
        } else {
            pause_continue.setOnClickListener(pause);
            pause_continue.setText("Pause");
            pause_continue.setBackgroundColor(Color.YELLOW);
        }
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.endTrip();
            }
        });

        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setState(BottomSheetBehavior.STATE_DRAGGING);
        behavior.setHideable(false);
        behavior.setPeekHeight(300);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
        if(map!=null){
            getInfoFromDb();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        map.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = googleMap;
        map.setLocationSource(this);
        map.setMyLocationEnabled(true);

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
        getInfoFromDb();

    }
    public void getInfoFromDb(){
        points = new LocationDbOpenHelper(getContext()).getTripPath(startDate, new Date().getTime());

        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(15);
        options.visible(true);
        options.addAll(points);
        if(!points.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : points) {
                builder.include(point);
            }
            int routePadding = 100;
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), routePadding));
        }else {
            @SuppressLint("MissingPermission") Location lastknown = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastknown.getLatitude(),lastknown.getLongitude())));
        }
        addedPolyline = map.addPolyline(options);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(listener != null){
            listener.onLocationChanged(location);
            if(addedPolyline != null && points != null){
                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
                addedPolyline.setPoints(points);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        listener = null;
    }
}
