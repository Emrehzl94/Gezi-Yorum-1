package com.example.murat.gezi_yorum.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import com.example.murat.gezi_yorum.classes.mLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;


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
                "("+COLUMN_ID+" integer auto_increment," +
                 COLUMN_STARTDATE +" date not null," +
                 COLUMN_FINISHDATE+" date)";
        db.execSQL(locationTableCreateQuery);
        db.execSQL(tripsTableCreateQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    public void saveLocation(mLocation location, SQLiteDatabase writableDatabase) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGTITUDE, location.getLongtitude());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_ALTITUDE, location.getAltitude());
        values.put(COLUMN_DATE, " time('now') ");
        writableDatabase.insert(TABLE_LOCATIONS, null, values);
    }
    public long insertStartEntry(){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATE," time('now') ");
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_TRIPS,null,values);
    }
    public boolean updateTripFinish(long inserted_trip_id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FINISHDATE," time('now') ");
        SQLiteDatabase database = getWritableDatabase();
        int effectedColumnCount = database.update(TABLE_TRIPS,values,COLUMN_ID+"='"+inserted_trip_id+"'",null);
        return effectedColumnCount > 0;
    }
    public PolylineOptions getTripInfo(String datestart, String dateend) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_LATITUDE + " ," + COLUMN_LONGTITUDE + " FROM " + TABLE_LOCATIONS + " WHERE "
                + COLUMN_DATE + ">='" + datestart + "' AND " + COLUMN_DATE + "<='" + dateend + "'";
        Cursor cursor = db.rawQuery(query, null);
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
}
