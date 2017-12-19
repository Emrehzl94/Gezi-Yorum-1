package com.example.murat.gezi_yorum;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

/**
 * Listens and saves locations to database. Must run as service.
 */

public class LocationSaveService extends Service implements LocationListener {
    private static final int MIN_TIME = 1000;
    private static final int MIN_DISTANCE = 3;
    public static LocationSaveService instance;

    private Path path;
    LocationManager locationManager;
    private LocationDbOpenHelper helper;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Konum takibi açık").
                setSmallIcon(R.mipmap.ic_launcher).
                setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            not.setChannelId(Constants.CH1);
        }
        startForeground(1, not.build());

        instance = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressWarnings("ConstantConditions") long path_id = intent.getExtras().getLong(Path.PATH_ID);
        helper = new LocationDbOpenHelper(this);
        path = helper.getPath(path_id);
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
        String type = path.calculateType();
        helper.updateTypeOfPath(path, type);
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
        //if(location.getAccuracy()<4)
        Toast.makeText(this, "Konum kaydedildi.", Toast.LENGTH_LONG).show();
            path.saveLocation(location);
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

