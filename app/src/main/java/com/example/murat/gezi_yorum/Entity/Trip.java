package com.example.murat.gezi_yorum.Entity;

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

    public  Trip(long id, long startdate, long finishdate, String note){
        this.id = id;
        this.startdate = startdate;
        this.finishdate = finishdate;
        this.name = note;
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
}
