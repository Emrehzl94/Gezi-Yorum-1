package com.example.murat.gezi_yorum.classes;

import java.util.Date;

/**
 * Custom Location struct for application.
 */

public class mLocation {
    private double Longtitude;
    private double Latitude;
    private double Altitude;
    private long Time;
    public mLocation(double Latitude,double Longtitude,double Altitude,long Time){
        this.Latitude = Latitude;
        this.Longtitude = Longtitude;
        this.Altitude = Altitude;
        this.Time = Time;
    }

    public double getLongtitude() {
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
}
