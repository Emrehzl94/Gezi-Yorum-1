package com.example.murat.gezi_yorum.Entity;

/**
 * Constants used frequently in application
 */

public class Constants {
    public static final String PREFNAME = "preferences";

    //meta files
    public static final String TRIP_META = "trip_metadata.JSON";
    public static final String PATH_META = "path_metadata.JSON";
    public static final String MEDIA_META = "media_metadata.JSON";

    //Server side constants
    public static final String ROOT = "http://192.168.1.35:8080";
    public static final String APP = "http://192.168.1.35:8080/Geziyorum/";
    //public static final String ROOT = "http://163.172.176.169:8080";
    //public static final String APP = "http://163.172.176.169:8080/Geziyorum/";
    public static final String PAGE = "page";
    public static final String HOME = "home";
    public static final String PROFILE = "profile";
    public static final String APPLICATION = "application";
    public static final String TRIPIDONSERVER = "tripIdOnServer";
    public static final String CHOSEN_TRIPID = "chosen_trip_id";

    //Activity messages
    public static final String STARTNEWTRIP = "start_new_trip"; //Start trip message send to Contiuning trip
    public static final String MESSAGE = "message"; // message tag

    //Address domains
    public static final String ADDRESS = "address";
    public static final String FEATURENAME = "feature_name"; // bilinen isim
    public static final String DISTRICT = "district"; // mahalle
    public static final String TOWN = "town"; // ilçe
    public static final String CITY = "city"; // şehir
    public static final String COUNTRY = "country"; // ülke

    public static final String CH1 = "channel1";
    public static final String CH2 = "channel2";
}
