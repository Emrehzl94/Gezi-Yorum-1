package com.example.murat.gezi_yorum.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import com.example.murat.gezi_yorum.classes.mLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by murat on 25.10.2017.
 */

public class LocationDbOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    private static final String TABLE_LOCATIONS = "Locations";
    private static final String COLUMN_LONGTITUDE = "longtitude";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_ALTITUDE = "altitude";
    private static final String COLUMN_FEATURE_NAME = "featureName";
    private static final String COLUMN_DATE = "date";

    private static final String TABLE_TRIPS = "Trips";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_STARTDATE = "startDate";
    private static final String COLUMN_FINISHDATE = "finishDate";

    public LocationDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String locationTableCreateQuery = "CREATE TABLE " + TABLE_LOCATIONS +
                " (" + COLUMN_LONGTITUDE + " double NOT NULL, " +
                COLUMN_LATITUDE + " double NOT NULL, " +
                COLUMN_ALTITUDE + " double NOT NULL," +
                COLUMN_FEATURE_NAME + " varchar(255)," +
                COLUMN_DATE + " date); ";
        String tripsTableCreateQuery = "CREATE TABLE " + TABLE_TRIPS +
                "("+COLUMN_ID+" integer PRIMARY KEY AUTOINCREMENT," +
                 COLUMN_STARTDATE +" date not null," +
                 COLUMN_FINISHDATE+" date)";
        db.execSQL(locationTableCreateQuery);
        db.execSQL(tripsTableCreateQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    public void saveLocation(mLocation location, SQLiteDatabase writableDatabase) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGTITUDE, location.getLongtitude());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_ALTITUDE, location.getAltitude());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        values.put(COLUMN_DATE, dateFormat.format(new Date()));
        writableDatabase.insert(TABLE_LOCATIONS, null, values);
    }
    public long insertStartEntry(){
        ContentValues values = new ContentValues();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        values.put(COLUMN_STARTDATE,dateFormat.format(new Date()));
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_TRIPS,null,values);
    }
    public boolean updateTripFinish(long inserted_trip_id){
        ContentValues values = new ContentValues();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        values.put(COLUMN_FINISHDATE,dateFormat.format(new Date()));
        SQLiteDatabase database = getWritableDatabase();
        int effectedColumnCount = database.update(TABLE_TRIPS,values,COLUMN_ID+"='"+inserted_trip_id+"'",null);
        return effectedColumnCount > 0;
    }
    public ArrayList<Integer> getTripsInfo(){
        String query = "SELECT "+COLUMN_ID+" FROM "+TABLE_TRIPS;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Integer> trip_ids = new ArrayList<>();
        int id_column_index = cursor.getColumnIndex(COLUMN_ID);
        while (!cursor.isAfterLast()) {
            trip_ids.add(cursor.getInt(id_column_index));
            cursor.moveToNext();
        }
        database.close();
        return trip_ids;
    }
    public PolylineOptions getTripInfo(int trip_id) {
        logInfoLocation();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_LATITUDE + " ," + COLUMN_LONGTITUDE +","+COLUMN_DATE+
                " FROM " + TABLE_LOCATIONS + " AS loc, "+ TABLE_TRIPS+" AS trip " +
                "WHERE " + COLUMN_DATE + ">= trip."+COLUMN_STARTDATE+" AND " + COLUMN_DATE+ "<= trip."+COLUMN_FINISHDATE
                + " AND trip."+COLUMN_ID+"='"+trip_id+"'";
        Log.d("Query", query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("Size", ""+cursor.getCount());
        cursor.moveToFirst();
        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(15);
        options.visible(true);
        while (!cursor.isAfterLast()) {
            options.add(new LatLng(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGTITUDE))));
            cursor.moveToNext();
        }
        db.close();
        return options;
    }
    public void logInfoTrip() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_TRIPS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d("ID", ""+cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            Log.d("STARTDATE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_STARTDATE)));
            Log.d("FINISHDATE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_FINISHDATE)));
            cursor.moveToNext();
        }
        db.close();
    }
    public void logInfoLocation() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_LOCATIONS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d("LATITUDE", ""+cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE)));
            Log.d("LONGITUDE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_LONGTITUDE)));
            cursor.moveToNext();
        }
        db.close();
    }
}
