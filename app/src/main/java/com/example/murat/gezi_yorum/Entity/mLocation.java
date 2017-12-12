package com.example.murat.gezi_yorum.Entity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Custom Location struct for carrying location data.
 */

public class mLocation {
    static final String LONGTITUDE = "longitude";
    static final String LATITUDE = "latitude";
    static final String ALTITUDE = "altitude";
    static final String TIME = "time";
    static final String ACCURACY = "accuracy";
    static final String SPEED = "speed";

    private double Longitude;
    private double Latitude;
    private double Altitude;
    private long Time;
    private float Speed;

    /**
     *
     * @param location android location object from location services
     */
    mLocation(Location location){
        this.Latitude = location.getLatitude();
        this.Longitude = location.getLongitude();
        this.Altitude = location.getAltitude();
        this.Time = location.getTime();
        this.Speed = location.getSpeed();
    }

    /**
     * This constructor used by MediaFile
     */
    mLocation(double Latitude, double Longitude, double Altitude, long Time){
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Altitude = Altitude;
        this.Time = Time;
    }

    /**
     * This constructor used by Path
     */
    mLocation(double Latitude, double Longitude, double Altitude, long Time, float speed){
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Altitude = Altitude;
        this.Time = Time;
        this.Speed = speed;
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

    float getSpeed() {
        return Speed;
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

    public LatLng convertLatLng(){
        return new LatLng(Latitude, Longitude);
    }
}
