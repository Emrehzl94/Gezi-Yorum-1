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
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

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

    private Boolean isTeamTrackEnabled;
    private Location lastLocation;
    private User user;
    private Trip trip;
    private Timer timer;
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

        isTeamTrackEnabled = intent.getExtras().getBoolean(Constants.LIVE_TRACK, false);
        if(isTeamTrackEnabled){
            user = new User(getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE));
            trip = helper.getTrip(intent.getExtras().getLong(Trip.TRIPID));
            timer = new Timer();
            timer.schedule(publishTask, 5000, 10000);
        }
        return START_STICKY;
    }
    public JSONArray lastknownteamlocation = null;
    TimerTask publishTask = new TimerTask() {
        @Override
        public void run() {
            String team_info = getTeamMembersLocation();
            try {
                JSONArray array = new JSONArray(team_info);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject member = array.getJSONObject(i);
                    if (member.getString("username").equals(user.username)) {
                        array.remove(i);
                        i--;
                    }
                }
                lastknownteamlocation = array;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public String getTeamMembersLocation(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection ConstantConditions
        if(cm.getActiveNetworkInfo() == null || lastLocation == null){
            return "";
        }
        JSONObject request = new JSONObject();
        try {
            request.put("token", user.token);
            JSONObject currentLocation = new JSONObject();
            currentLocation.put("tripId", trip.idOnServer);
            currentLocation.put("username", user.username);
            currentLocation.put("longitude", lastLocation.getLongitude());
            currentLocation.put("latitude", lastLocation.getLatitude());
            currentLocation.put("altitude", lastLocation.getAltitude());
            request.put("currentLocation", currentLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        URLRequestHandler urlRequestHandler = new URLRequestHandler(request.toString(),Constants.APP+"saveLocation");
        if(urlRequestHandler.getResponseMessage()){
            return urlRequestHandler.getResponse();
        }
        return "";
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        String type = path.calculateType();
        helper.updateTypeOfPath(path, type);
        Toast.makeText(this,"Service stopped",Toast.LENGTH_LONG).show();
        instance = null;
        timer.cancel();
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy()<40) {
            lastLocation = location;
            path.saveLocation(location);
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
}

