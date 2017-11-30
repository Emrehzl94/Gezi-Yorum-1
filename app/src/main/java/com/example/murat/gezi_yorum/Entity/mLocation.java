package com.example.murat.gezi_yorum.Entity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Custom Location struct for carrying location data.
 */

public class mLocation {
    public static final String LONGTITUDE = "longtitude";
    public static final String LATITUDE = "latitude";
    public static final String ALTITUDE = "altitude";
    public static final String TIME = "time";

    private double Longitude;
    private double Latitude;
    private double Altitude;
    private long Time;
    private float Accuracy;
    private float Speed;
    public mLocation(Location location){
        this.Latitude = location.getLatitude();
        this.Longitude = location.getLongitude();
        this.Altitude = location.getAltitude();
        this.Time = location.getTime();
        this.Accuracy = location.getAccuracy();
        this.Speed = location.getSpeed();
    }
    mLocation(double Latitude, double Longitude, double Altitude, long Time){
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Altitude = Altitude;
        this.Time = Time;
    }

    public double getLongitude() {
        return Longitude;
    }

    public long getTime() {
        return Time;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public double distInMeters(mLocation location) {
        double lat2 = location.getLatitude();
        double lng2 = location.getLongitude();
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2- Latitude);
        double dLng = Math.toRadians(lng2- Longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(Latitude)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (earthRadius * c);
    }

    LatLng convertLatLng(){
        return new LatLng(Latitude, Longitude);
    }
}
