package com.example.murat.gezi_yorum.classes;

import com.google.android.gms.maps.model.LatLng;

/**
 * Custom Location struct for carrying location data.
 */

public class mLocation {
    public static final String LONGTITUDE = "longtitude";
    public static final String LATITUDE = "latitude";
    public static final String ALTITUDE = "altitude";
    public static final String TIME = "time";

    private double Longtitude;
    private double Latitude;
    private double Altitude;
    private long Time;
    public mLocation(double Latitude,double Longitude,double Altitude,long Time){
        this.Latitude = Latitude;
        this.Longtitude = Longitude;
        this.Altitude = Altitude;
        this.Time = Time;
    }

    public double getLongitude() {
        return Longtitude;
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

    LatLng convertLatLng(){
        return new LatLng(Latitude,Longtitude);
    }
}
