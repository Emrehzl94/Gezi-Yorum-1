package com.example.murat.gezi_yorum.Entity;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.example.murat.gezi_yorum.Entity.mLocation;
import com.example.murat.gezi_yorum.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Saves locations to CSV files and get them Path file manager
 */

public class Path {
    private File file;
    private long path_id;
    private long startdate;
    private long finishdate;
    private String type;
    private ArrayList<mLocation> locations;

    public static final String PATH_ID = "path_id";
    //Path types
    public static final String WALK = "walk";
    public static final String RUN = "run";
    public static final String RIDE = "ride";
    public static final String CAR = "car";

    public Path(long path_id, long startdate, long finishdate, String path_file, String type){
        this.path_id = path_id;
        this.startdate = startdate;
        this.finishdate = finishdate;
        this.type = type;
        this.file = new File(path_file);
        if(!file.exists()) {
           create();
        }
        loadLocations();
    }
    public static String getRouteFilePath(Context context, long time){
        return context.getFilesDir() + "/path_"+time+".csv";
    }
    public File getFile() {
        return file;
    }
    public long getPath_id() {
        return path_id;
    }
    public int getLocationsSize(){
        return locations.size();
    }
    private void create(){
        try {
            if(file.createNewFile()) {
                PrintWriter writer = new PrintWriter(new FileOutputStream(file));
                writer.println(getHeader());
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getHeader(){
        return mLocation.LATITUDE + "," + mLocation.LONGTITUDE +","+mLocation.ALTITUDE+","+mLocation.TIME
        +","+mLocation.ACCURACY+","+mLocation.SPEED;
    }
    public void saveLocation(Location location){
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(file,true));
            writer.println(location.getLatitude()+","+location.getLongitude()+","+location.getAltitude()+","+location.getTime()
            +","+location.getAccuracy()+","+location.getSpeed());
            writer.close();
            locations.add(new mLocation(location));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void loadLocations(){
        locations = new ArrayList<>();
        try {
            Scanner fileScanner = new Scanner(file);
            if(fileScanner.hasNext() && !fileScanner.nextLine().equals(getHeader())){
                return;
            }
            while (fileScanner.hasNextLine()){
                String line = fileScanner.nextLine();
                StringTokenizer tokenizer = new StringTokenizer(line,",");
                locations.add(new mLocation(
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Long.parseLong(tokenizer.nextToken()),
                        Float.parseFloat(tokenizer.nextToken())
                ));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<LatLng> getLocationsAsLatLng(){
        ArrayList<LatLng> locationsAsLatLng = new ArrayList<>();
        for (mLocation location : locations){
            locationsAsLatLng.add(location.convertLatLng());
        }
        return locationsAsLatLng;
    }

    /**
     * Returns only first location of this trip
     * @return first location
     */
    private LatLng peekFirst(){
        if(locations.size()>0){
            return locations.get(0).convertLatLng();
        }
        return null;
    }

    public JSONObject toJSONObject(Geocoder geocoder){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("start_date", startdate);
            jsonObject.put("finish_date",finishdate);
            jsonObject.put("file", file.getName());
            jsonObject.put("type", type);
            LatLng location = peekFirst();
            if(location != null){
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                    JSONObject address = new JSONObject();
                    address.put(Constants.FEATURENAME, addresses.get(0).getFeatureName());
                    address.put(Constants.DISTRICT, addresses.get(0).getSubLocality());
                    address.put(Constants.TOWN, addresses.get(0).getSubAdminArea());
                    address.put(Constants.CITY, addresses.get(0).getAdminArea());
                    address.put(Constants.COUNTRY, addresses.get(0).getCountryName());
                    jsonObject.put(Constants.ADDRESS, address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String calculateType(){
        float averageSpeed = calculateAverageSpeed();
        String type;
        if(averageSpeed<1.5){
            type = Path.WALK;
        }else if(averageSpeed < 3.5){
            type = Path.RUN;
        }else if(averageSpeed < 6.5){
            type = Path.RIDE;
        }else {
            type = Path.CAR;
        }
        return type;
    }

    private float calculateAverageSpeed(){
        float totalSpeed = 0;
        for (mLocation location : locations){
            totalSpeed += location.getSpeed();
        }
        return  totalSpeed/(float) locations.size();
    }
    public Polyline drawOnMap(GoogleMap map, List<LatLng> points, Boolean isImported){
        int color = isImported ? Color.BLUE : Color.RED;
        PolylineOptions options = new PolylineOptions();
        options.color(color);
        options.width(15);
        options.visible(true);
        options.addAll(points);
        return map.addPolyline(options);
    }
}
