package com.example.murat.gezi_yorum.Entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.StringTokenizer;

/**
 * Trip object
 */

public class Trip {
    public long id;
    private long startdate;
    private long finishdate;
    public String name;
    private String members;

    public  Trip(long id, long startdate, long finishdate, String note, String members){
        this.id = id;
        this.startdate = startdate;
        this.finishdate = finishdate;
        this.name = note;
        this.members = members;
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
            JSONArray membersAsJSON = new JSONArray();
            StringTokenizer tokenizer = new StringTokenizer(members, ",");
            while (tokenizer.hasMoreTokens()){
                membersAsJSON.put(tokenizer.nextToken());
            }
            jsonObject.put("startdate",startdate);
            jsonObject.put("finishdate", finishdate);
            jsonObject.put("name", name);
            jsonObject.put("members", membersAsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
