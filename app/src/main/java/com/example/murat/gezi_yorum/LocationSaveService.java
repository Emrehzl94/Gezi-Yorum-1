package com.example.murat.gezi_yorum;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Utils.LocationCSVHandler;

/**
 * Listens and saves locations to database. Must run as service.
 */

public class LocationSaveService extends Service implements LocationListener {
    private static final int MIN_TIME = 1000;
    private static final int MIN_DISTANCE = 3;
    public static LocationSaveService instance;

    private LocationCSVHandler csvHandler;
    LocationManager locationManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Konum takibi açık").
                setSmallIcon(R.mipmap.ic_launcher).
                setContentIntent(pendingIntent).build();
        startForeground(1, not);

        instance = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        long trip_id = intent.getExtras().getLong(Constants.TRIPID);
        long path_id = intent.getExtras().getLong(Constants.PATH_ID);
        csvHandler = new LocationCSVHandler(trip_id, path_id, getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Service cannot started. Location permission is not granted.", Toast.LENGTH_LONG).show();
            this.stopSelf();
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        Toast.makeText(this,"Service stopped",Toast.LENGTH_LONG).show();
        instance = null;
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy()<4)
            csvHandler.saveLocation(location);
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

