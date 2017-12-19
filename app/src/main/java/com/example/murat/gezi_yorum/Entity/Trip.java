package com.example.murat.gezi_yorum.Entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

/**
 * Trip object
 */

public class Trip {
    public long id;
    private long startdate;
    private long finishdate;
    public String name;
    private String members;
    public Long idOnServer;
    public Boolean isImported;
    public Boolean isCreator;
    public Boolean isShared;

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
    public static final String CREATOR = "creator";

    public  Trip(long id, long startdate, long finishdate, String name, int isImported,String members, Long idOnServer, String isCreator, String isShared){
        this.id = id;
        this.startdate = startdate;
        this.finishdate = finishdate;
        this.name = name;
        this.members = members;
        this.idOnServer = idOnServer;
        this.isImported = isImported != 0;
        this.isCreator = Boolean.parseBoolean(isCreator);
        this.isShared = Boolean.parseBoolean(isShared);
    }

    public String getStartdate() {
        return DateFormat.getDateInstance().format(startdate);
    }

    public String getFinishdate() {
        return DateFormat.getDateInstance().format(finishdate);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("startdate",startdate);
            jsonObject.put("finishdate", finishdate);
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean isGroupTrip(){
        return idOnServer != -1;
    }
}
