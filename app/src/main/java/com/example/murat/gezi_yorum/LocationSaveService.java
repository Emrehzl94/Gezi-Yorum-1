package com.example.murat.gezi_yorum;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.murat.gezi_yorum.classes.LocationCSVHandler;
import com.example.murat.gezi_yorum.classes.mLocation;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Listens and saves locations to database. Must run as service.
 */

public class LocationSaveService extends Service implements LocationListener {
    private static final int MIN_TIME = 10000;
    private static final int MIN_DISTANCE = 3;
    public static LocationSaveService instance;

    private LocationCSVHandler csvHandler;
    LocationManager locationManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        long trip_id = intent.getExtras().getLong("trip_id");
        csvHandler = new LocationCSVHandler(trip_id,getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Service cannot started. Location permission is not granted.", Toast.LENGTH_LONG).show();
            this.stopSelf();
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        Toast.makeText(getApplicationContext(),"Service stopped",Toast.LENGTH_LONG).show();
        instance = null;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        Double altitude = location.getAltitude();
        csvHandler.saveLocation(new mLocation(latitude,longitude,altitude,System.currentTimeMillis()));
        Toast.makeText(getApplicationContext(),"Konum kaydedildi.",Toast.LENGTH_SHORT).show();
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
}

