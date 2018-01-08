package com.example.murat.gezi_yorum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MultipartUtility;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates ZipFiles and upload them
 */

public class ZipFileUploader extends Service {
    private File zipFile;
    private Trip trip;
    private LocationDbOpenHelper helper;

    private int notificationId = 1000;

    NotificationManager manager;
    private Notification.Builder not;

    private Boolean isValidToShare = false;
    private Handler handler;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Bundle extras = intent.getExtras();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection ConstantConditions
        if(cm.getActiveNetworkInfo() == null){
            Toast.makeText(this, getString(R.string.internet_warning),
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Dosyalar hazırlanıyor.").
                setSmallIcon(R.drawable.ic_stat_notification).
                setProgress(100,0,false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            not.setChannelId(Constants.CH2);
        }
        startForeground(notificationId, not.build());

        this.zipFile = new File(getFilesDir()+"/trip.zip");
        helper = new LocationDbOpenHelper(getApplicationContext());
        //noinspection ConstantConditions
        Long trip_id = extras.getLong(Trip.TRIPID);
        trip = helper.getTrip(trip_id);
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                create();
                if(isValidToShare) {
                    if(zipFile.length() > 1024*1024*100){ // 100 MB
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.app_name));
                        builder.setSmallIcon(R.drawable.ic_stat_notification);
                        builder.setContentTitle(getString(R.string.too_big_to_share));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            builder.setChannelId(Constants.CH1);
                        }
                        manager.notify(0, builder.build());
                        if(zipFile != null && zipFile.exists()){
                            zipFile.delete();
                        }
                        stopSelf();
                        return;
                    }
                    upload();
                }else {
                    if(zipFile != null && zipFile.exists()){
                        zipFile.delete();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.app_name));
                            builder.setSmallIcon(R.drawable.ic_stat_notification);
                            builder.setContentTitle(getString(R.string.not_suitable_for_share));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                builder.setChannelId(Constants.CH1);
                            }
                            manager.notify(0, builder.build());
                            Toast.makeText(getApplicationContext(),
                                    R.string.not_suitable_for_share, Toast.LENGTH_LONG).show();
                        }
                    });
                    stopSelf();
                }
            }
        }).start();

        return START_STICKY;
    }
    private void upload(){
        not.setContentTitle(getString(R.string.uploading_trip));
        manager.notify(notificationId,not.build());
        String service = trip.isGroupTrip() ? "uploadGroupTripZip" : "uploadTripZip";
        String URL = Constants.APP + service;
        SharedPreferences preferences = getSharedPreferences(Constants.PREFNAME,Context.MODE_PRIVATE);
        User user = new User(preferences);

        String message ="";
        try {
            MultipartUtility multipart = new MultipartUtility(URL,"UTF-8", false);
            multipart.addFormField("token", user.token);
            multipart.addFormField("fileName",zipFile.getName());
            multipart.addFilePart("file",zipFile);
            if(trip.isGroupTrip()){
                multipart.addFormField("tripId", trip.idOnServer.toString());
                multipart.addFormField("isFirstFileUpload", trip.isCreator.toString());
            }
            List<String> response_messages = multipart.finish();
            helper.tripIsShared(trip.id);
            message = getString(R.string.upload_successful);
        } catch (Exception e) {
            e.printStackTrace();
            message = getString(R.string.error);
        }finally {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.app_name));
            builder.setSmallIcon(R.drawable.ic_stat_notification);
            builder.setContentTitle(message);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(Constants.CH1);
            }
            manager.notify(0, builder.build());
            zipFile.delete();
            stopSelf();
        }
    }
    private void create(){
        try {
            if(!zipFile.exists()){
                //noinspection ResultOfMethodCallIgnored
                zipFile.createNewFile();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            if(!trip.isGroupTrip() || trip.isCreator) {
                // Trip meta preparing and writing to zip
                ZipEntry tripMetaDataEntry = new ZipEntry(Constants.TRIP_META);
                zipOutputStream.putNextEntry(tripMetaDataEntry);
                zipOutputStream.write(trip.toJSONObject().toString().getBytes());

                if(trip.cover_media_id != -1){
                    ZipEntry coverEntry = new ZipEntry("kapak.jpg");
                    zipOutputStream.putNextEntry(coverEntry);
                    writeByteArrayOriginal(new File(helper.getMediaFile(trip.cover_media_id).path),zipOutputStream);
                }

                Geocoder geocoder;
                geocoder = new Geocoder(this, Locale.getDefault());

                // Path metadata preparing and path and path metadata writing to zip
                ArrayList<Long> pathIDs = helper.getPathsIDs(trip.id);
                JSONArray pathMetaData = new JSONArray();
                for (Long pathId : pathIDs) {
                    Path path = helper.getPath(pathId);
                    if(path.getLocationsSize()>1) {
                        isValidToShare = true;
                        File pathFile = path.getFile();

                        ZipEntry pathEntry = new ZipEntry("Paths/" + pathFile.getName());
                        zipOutputStream.putNextEntry(pathEntry);
                        RandomAccessFile randomAccessFile = new RandomAccessFile(pathFile, "r");
                        byte[] bytes = new byte[(int) randomAccessFile.length()];
                        randomAccessFile.readFully(bytes);
                        zipOutputStream.write(bytes);

                        pathMetaData.put(path.toJSONObject(geocoder));
                    }
                }
                ZipEntry pathMetaDataEntry = new ZipEntry(Constants.PATH_META);
                zipOutputStream.putNextEntry(pathMetaDataEntry);
                zipOutputStream.write(pathMetaData.toString().getBytes());
            }
            if(!isValidToShare) return;
            // Media metadata preparing and media and media metadata writing to zip
            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip.id,null,
                    " AND " + LocationDbOpenHelper.COLUMN_SHARE_OPTION +"!=\""+MediaFile.ONLY_ME+"\"", null);
            JSONArray mediaMetaData = new JSONArray();
            int fileCount = 0;
            for (MediaFile file: mediaFiles){
                not.setContentTitle((fileCount++)+"/"+mediaFiles.size());
                manager.notify(notificationId, not.build());
                File entryFile = new File(file.path);
                ZipEntry mediaFileEntry = new ZipEntry("Media/"+entryFile.getName());
                zipOutputStream.putNextEntry(mediaFileEntry);
                writeByteArrayOriginal(entryFile,zipOutputStream);
                mediaMetaData.put(file.toJSONObject());
            }
            ZipEntry mediaMetaDataEntry = new ZipEntry(Constants.MEDIA_META);
            zipOutputStream.putNextEntry(mediaMetaDataEntry);
            zipOutputStream.write(mediaMetaData.toString().getBytes());
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeByteArrayOriginal(File file, OutputStream zipOutputStream){
        InputStream ios = null;
        try {
            byte[] buffer = new byte[1048576];// 1MB
            ios = new FileInputStream(file);
            int read;
            int written = 0;
            Integer length = Integer.parseInt(Long.valueOf(file.length()).toString());
            while ((read = ios.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, read);
                not.setProgress(100, 100 * written/length, false);
                manager.notify(notificationId, not.build());
                written += read;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ios != null) ios.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
