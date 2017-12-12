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
    //public static final String ROOT = "http://192.168.1.33:8080";
    //public static final String APP = "http://192.168.1.33:8080/Geziyorum/";
    public static final String ROOT = "http://163.172.176.169:8080";
    public static final String APP = "http://163.172.176.169:8080/Geziyorum/";
    public static final String PAGE = "page";
    public static final String HOME = "home";
    public static final String PROFILE = "profile";
    public static final String TOKEN = "access_token";
    public static final String APPLICATION = "application";
    public static final String PROFILEPHOTO = "profile_photo";
    public static final String USERNAME = "username";
    public static final String NAME_SURNAME = "name_surname";

    //Activity messages
    public static final String STARTNEWTRIP = "start_new_trip"; //Start trip message send to Contiuning trip
    public static final String MESSAGE = "message"; // message tag
    public static final String PATH_ID = "path_id";
    public static final String TRIPNAME = "trip_name";
    public static final String MEMBERS = "members";

    //Trip flags
    public static final String TRIPSTATE = "trip_state"; // is trip started or ended
    public static final String STARTED = "started";
    public static final String ENDED = "ended";
    public static final String RECORDSTATE = "record_state"; // ist recording must be active or passive
    public static final String ACTIVE = "active";
    public static final String PASSIVE = "passive";
    public static final String TRIPID = "trip_id";

    // Media types
    public static final String PHOTO = "photo";
    public static final String VIDEO = "video";
    public static final String SOUNDRECORD = "record";

    //Share options
    public static final String SHARE_OPTION = "share_option"; // Media share option chosen from user
    public static final String EVERYBODY = "everybody";
    public static final String MY_FRIENDS = "only_friends";
    public static final String ONLY_ME = "only_me";

    //Address domains
    public static final String ADDRESS = "address";
    public static final String FEATURENAME = "feature_name"; // bilinen isim
    public static final String DISTRICT = "district"; // mahalle
    public static final String TOWN = "town"; // ilçe
    public static final String CITY = "city"; // şehir
    public static final String COUNTRY = "country"; // ülke

    //Path types
    public static final String WALK = "walk";
    public static final String RUN = "run";
    public static final String RIDE = "ride";
    public static final String CAR = "car";

}
