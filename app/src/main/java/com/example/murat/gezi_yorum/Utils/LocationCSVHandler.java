package com.example.murat.gezi_yorum.Utils;

import android.content.Context;

import com.example.murat.gezi_yorum.Entity.mLocation;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Saves locations to CSV files and get them
 */

public class LocationCSVHandler {
    private File file;
    private long trip_id;
    public long path_id;
    public LocationCSVHandler(long trip_id, long path_id, Context context){
        this.trip_id = trip_id;
        this.path_id = path_id;
        this.file = getRouteFilePath(trip_id, path_id, context);
        if(!file.exists()){
            create();
        }
    }
    public static File getRouteFilePath(long trip_id, long path_id, Context context){
        return new File(context.getFilesDir() + "path_"+trip_id+"_"+path_id+".csv");
    }
    private void create(){
        try {
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            writer.println(getHeader());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getHeader(){
        return mLocation.LATITUDE + "," + mLocation.LONGTITUDE +","+mLocation.ALTITUDE+","+mLocation.TIME;
    }
    public void saveLocation(mLocation location){
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(file,true));
            writer.println(location.getLatitude()+","+location.getLongitude()+","+location.getAltitude()+","+location.getTime());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<LatLng> getLocations(){
        ArrayList<LatLng> locations = new ArrayList<>();
        try {
            Scanner fileScanner = new Scanner(file);
            if(!fileScanner.nextLine().equals(getHeader())){
                return locations;
            }
            while (fileScanner.hasNextLine()){
                String line = fileScanner.nextLine();
                StringTokenizer tokenizer = new StringTokenizer(line,",");
                LatLng latLng = new LatLng(Double.parseDouble(tokenizer.nextToken()), Double.parseDouble(tokenizer.nextToken()));
                locations.add(latLng);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return locations;
    }
}
