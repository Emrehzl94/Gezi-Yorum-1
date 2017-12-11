package com.example.murat.gezi_yorum.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Performs database operations.
 */

public class LocationDbOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";


    public static final String COLUMN_ID = "id";

    public static final String TABLE_TRIPS = "Trips";
    public static final String COLUMN_STARTDATE = "startDate";
    public static final String COLUMN_FINISHDATE = "finishDate";
    public static final String COLUMN_NAME = "trip_name";
    public static final String COLUMN_ISIMPORTED = "is_imported";

    public static final String TABLE_PATHS = "Paths";

    public static final String TABLE_MEDIA = "Media";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_LONGTITUDE= "longtitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_ALTITUDE = "altitude";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_TRIPID = "trip_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_THUMBNAIL = "thumbnail";
    public static final String COLUMN_SHARE_OPTION = "share_option";
    public static final String COLUMN_NOTE = "name";


    public LocationDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tripsTableCreateQuery = "CREATE TABLE " + TABLE_TRIPS +
                "("+COLUMN_ID+" integer PRIMARY KEY AUTOINCREMENT," +
                 COLUMN_STARTDATE +" INTEGER not null," +
                 COLUMN_FINISHDATE+" INTEGER not null," +
                 COLUMN_NAME+" text," +
                 COLUMN_ISIMPORTED+" INTEGER not null)";
        String pathTableCreateQuery = "CREATE TABLE " + TABLE_PATHS +
                "("+COLUMN_ID+" integer PRIMARY KEY AUTOINCREMENT," +
                COLUMN_STARTDATE +" INTEGER not null," +
                COLUMN_FINISHDATE+" INTEGER not null," +
                COLUMN_TRIPID + " INTEGER not null)";
        String mediaTableCreateQuery = "CREATE TABLE " + TABLE_MEDIA +
                " (" + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TYPE +" varchar(255)," +
                COLUMN_LONGTITUDE + " double NOT NULL, " +
                COLUMN_LATITUDE + " double NOT NULL, " +
                COLUMN_ALTITUDE + " double NOT NULL," +
                COLUMN_PATH + " varchar(255)," +
                COLUMN_TRIPID + " integer,"+
                COLUMN_DATE + " integer," +
                COLUMN_THUMBNAIL+" blob NOT NULL,"+
                COLUMN_SHARE_OPTION+" text NOT NULL," +
                COLUMN_NOTE+" text); ";
        db.execSQL(tripsTableCreateQuery);
        db.execSQL(pathTableCreateQuery);
        db.execSQL(mediaTableCreateQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATHS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        onCreate(db);
    }

    /**
     * Inserts new trip to database created by user
     * @return trip_id
     */
    //TODO: Add new ArrayList<Long> user_ids to this trip
    public long startNewTrip(String name){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATE,System.currentTimeMillis());
        values.put(COLUMN_FINISHDATE,Long.MAX_VALUE);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ISIMPORTED, 0);
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_TRIPS,null ,values);
    }
    /**
     * Inserts new trip to database created by user
     * @return trip_id
     */
    //TODO: Add new ArrayList<Long> user_ids to this trip
    public long importTrip(long startdate, long finishdate, String name){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATE,startdate);
        values.put(COLUMN_FINISHDATE,finishdate);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ISIMPORTED, 1);
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_TRIPS,null ,values);
    }

    public void updateTripName(Long trip_id, String name){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,name);
        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_TRIPS, values, COLUMN_ID + "=" +trip_id, null);
    }
    /**
     * Ends trip
     * @param path_id path id
     */
    public void endTrip(long path_id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FINISHDATE,System.currentTimeMillis());
        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_TRIPS, values, COLUMN_ID + "=" + path_id, null);
    }

    /**
     * Starts new path recording
     * @param trip_id trip id
     * @return path_id
     */
    public long startNewPath(long trip_id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATE,System.currentTimeMillis());
        values.put(COLUMN_FINISHDATE,Long.MAX_VALUE);
        values.put(COLUMN_TRIPID,trip_id);
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_PATHS,null ,values);
    }

    /**
     * Ends path
     * @param trip_id trip id
     */
    public void endPath(long trip_id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FINISHDATE,System.currentTimeMillis());
        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_PATHS, values, COLUMN_ID + "=" + trip_id, null);
    }

    /**
     * Returns created trip_ids from user
     * @return trip_ids created from user
     */
    public ArrayList<Long> getTripsIDsForTimeLine(){
        String query = "SELECT "+COLUMN_ID+" FROM "+TABLE_TRIPS +" WHERE "+
                COLUMN_FINISHDATE+"!="+Long.MAX_VALUE +" AND "+COLUMN_ISIMPORTED+"=0";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Long> trip_ids = new ArrayList<>();
        int id_column_index = cursor.getColumnIndex(COLUMN_ID);
        while (!cursor.isAfterLast()) {
            trip_ids.add(cursor.getLong(id_column_index));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return trip_ids;
    }
    /**
     * Returns created trip_ids from user
     * @return trip_ids created from user
     */
    public ArrayList<Trip> getTrips(){
        String query = "SELECT * FROM "+TABLE_TRIPS +" WHERE "+
                COLUMN_FINISHDATE+"!="+Long.MAX_VALUE +" AND "+COLUMN_ISIMPORTED+"=0";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Trip> trips = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            trips.add(new Trip(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_STARTDATE)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_FINISHDATE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            ));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return trips;
    }
    /**
     * Returns created trip_ids from user
     * @return trip_ids created from user
     */
    public ArrayList<Trip> getImportedTrips(){
        String query = "SELECT "+COLUMN_ID+" FROM "+TABLE_TRIPS +" WHERE "+COLUMN_ISIMPORTED+"=1";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Trip> trips = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            trips.add(new Trip(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_STARTDATE)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_FINISHDATE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            ));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return trips;
    }

    /**
     * Returns paths in a trip
     * @param trip_id trip_id
     * @return path_ids in given trip id
     */
    public ArrayList<Long> getPathsIDs(long trip_id){
        String query = "SELECT "+COLUMN_ID+" FROM "+TABLE_PATHS +
                " WHERE "+COLUMN_TRIPID+"="+trip_id;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Long> path_ids = new ArrayList<>();
        int id_column_index = cursor.getColumnIndex(COLUMN_ID);
        while (!cursor.isAfterLast()) {
            path_ids.add(cursor.getLong(id_column_index));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return path_ids;
    }

    /**
     * Returns Trip object for given trip_id
     * @param trip_id trip_id
     * @return Trip object
     */
    public Trip getTrip(long trip_id){
        String query = "SELECT * FROM "+TABLE_TRIPS+" WHERE "+COLUMN_ID+"="+trip_id;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        Trip trip = new Trip(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_STARTDATE)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_FINISHDATE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
        );
        cursor.close();
        database.close();
        return trip;
    }

    /**
     * Returns pathInfo for given path_id to write on zip file as JSON
     * @param path_id path_id
     * @return jsonObject of path
     */
    public JSONObject getPathInfo(long path_id){
        String query = "SELECT * FROM "+TABLE_PATHS+" WHERE "+COLUMN_ID+"='"+path_id+"'";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();

        JSONObject jsonObject = new JSONObject();
        long trip_id = cursor.getLong(cursor.getColumnIndex(COLUMN_TRIPID));
        try {
            jsonObject.put("start_date",cursor.getLong(cursor.getColumnIndex(COLUMN_STARTDATE)));
            jsonObject.put("finish_date",cursor.getLong(cursor.getColumnIndex(COLUMN_FINISHDATE)));
            jsonObject.put("file", "path_"+trip_id+"_"+path_id+".csv");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cursor.moveToNext();
        cursor.close();
        database.close();
        return jsonObject;
    }

    /**
     * Inserts media file to database
     * @param mediaFile mediaFile to be save
     */
    public void insertMediaFile(MediaFile mediaFile){
        ContentValues values = new ContentValues();

        values.put(COLUMN_TYPE,mediaFile.type);
        values.put(COLUMN_PATH,mediaFile.path);
        values.put(COLUMN_LATITUDE,mediaFile.location.getLatitude());
        values.put(COLUMN_LONGTITUDE, mediaFile.location.getLongitude());
        values.put(COLUMN_ALTITUDE, mediaFile.location.getAltitude());
        values.put(COLUMN_TRIPID, mediaFile.trip_id);
        values.put(COLUMN_DATE,mediaFile.location.getTime());
        values.put(COLUMN_THUMBNAIL,mediaFile.getByteArray());
        values.put(COLUMN_SHARE_OPTION,mediaFile.share_option);
        SQLiteDatabase database = getWritableDatabase();
        mediaFile.id = database.insert(TABLE_MEDIA, null, values);
    }

    /**
     * Returns media files taken in trip
     * @param trip_id Trip id
     * @param type Media Type
     * @param additionalQuery additional SQL query
     * @return mediaFiles
     */
    public ArrayList<MediaFile> getMediaFiles(long trip_id, @Nullable String type ,@Nullable String additionalQuery) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_MEDIA+" WHERE "+COLUMN_TRIPID+"='"+trip_id+"'";
        if(type != null){
            query+=" AND "+ COLUMN_TYPE + "='" +type+"'";
        }
        if (additionalQuery != null){
            query += additionalQuery;
        }
        query += " ORDER BY "+ COLUMN_DATE + " DESC, "+COLUMN_LATITUDE +","+COLUMN_LONGTITUDE;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<MediaFile> media = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            media.add(createMediaFileFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return media;
    }

    /**
     * Return MediaFile object in given media_id
     * @param media_id media_id
     * @return MediaFile object
     */
    public MediaFile getMediaFile(Long media_id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_MEDIA+" WHERE "+COLUMN_ID+"='"+media_id+"'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        MediaFile mediaFile = null;
        if (!cursor.isAfterLast()) {
            mediaFile = createMediaFileFromCursor(cursor);
        }
        cursor.close();
        db.close();
        return mediaFile;
    }

    /**
     * Removes media file from database
     * @param media_id media_id
     */
    public void deleteMediaFile(Long media_id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM "+TABLE_MEDIA+" WHERE "+COLUMN_ID+"='"+media_id+"'";
        db.execSQL(query);
    }

    public void updateMediaNote(MediaFile mediaFile, String note){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE,note);
        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_MEDIA, values, COLUMN_ID + "=" + mediaFile.id, null);
        mediaFile.about_note = note;
    }

    public void updateShareOption(MediaFile mediaFile, String option){
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHARE_OPTION,option);
        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_MEDIA, values, COLUMN_ID + "=" + mediaFile.id, null);
        mediaFile.about_note = option;
    }

    private MediaFile createMediaFileFromCursor(Cursor cursor){
        return new MediaFile(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_PATH)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGTITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_ALTITUDE)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_TRIPID)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)),
                cursor.getBlob(cursor.getColumnIndex(COLUMN_THUMBNAIL)),
                cursor.getString(cursor.getColumnIndex(COLUMN_SHARE_OPTION)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NOTE))
        );
    }
}
