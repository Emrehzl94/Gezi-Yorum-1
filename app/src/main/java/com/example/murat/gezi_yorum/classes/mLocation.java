package com.example.murat.gezi_yorum.classes;

import java.util.Date;

/**
 * Created by murat on 25.10.2017.
 */

public class mLocation {
    private double Longtitude;
    private double Latitude;
    private double Altitude;
    private String Time;
    public mLocation(double Latitude,double Longtitude,double Altitude,String Time){
        this.Latitude = Latitude;
        this.Longtitude = Longtitude;
        this.Altitude = Altitude;
        this.Time = Time;
    }

    public double getLongtitude() {
        return Longtitude;
    }

    public String getTime() {
        return Time;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getAltitude() {
        return Altitude;
    }
}
