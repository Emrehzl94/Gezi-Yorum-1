package com.example.murat.gezi_yorum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MultipartUtility;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private long trip_id;
    private LocationDbOpenHelper helper;

    private int notificationId = 1000;

    NotificationManager manager;
    private Notification.Builder not;
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
        //noinspection ConstantConditions
        trip_id = extras.getLong(Constants.TRIPID);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Dosyalar hazırlanıyor.").
                setSmallIcon(R.mipmap.ic_launcher).
                setProgress(100,0,false);
        startForeground(notificationId, not.build());

        this.zipFile = new File(Environment.getExternalStoragePublicDirectory(getApplicationContext().getString(R.string.app_name)) + "/trip_"+trip_id+".zip");
        helper = new LocationDbOpenHelper(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                create();
                upload();
            }
        }).start();

        return START_STICKY;
    }
    private void upload(){
        not.setContentTitle("Dosya yükleniyor...");
        manager.notify(notificationId,not.build());
        String URL = "http://163.172.176.169:8080/Geziyorum/uploadFile";
        String message;
        try {
            MultipartUtility multipart = new MultipartUtility(URL,"UTF-8", false);
            multipart.addFormField("name",zipFile.getName());
            multipart.addFilePart("file",zipFile);
            List<String> response_messages = multipart.finish();
            for(String response_message : response_messages){
                Log.w("Respons", "upload: " + response_message);
            }
            message = "Dosyalar başarıyla yüklendi.";
        } catch (Exception e) {
            e.printStackTrace();
            message = "Dosya yüklemesi sırasında hata meydana geldi..";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(message);
        manager.notify(0, builder.build());
        stopSelf();

    }
    private void create(){
        try {
            if(!zipFile.exists()){
                //noinspection ResultOfMethodCallIgnored
                zipFile.createNewFile();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            // Trip meta preparing and writing to zip
            Trip trip = helper.getTrip(trip_id);
            ZipEntry tripMetaDataEntry = new ZipEntry(Constants.TRIP_META);
            zipOutputStream.putNextEntry(tripMetaDataEntry);
            zipOutputStream.write(trip.toJSONObject().toString().getBytes());

            Geocoder geocoder;
            geocoder = new Geocoder(this, Locale.getDefault());

            // Path metadata preparing and path and path metadata writing to zip
            ArrayList<Long> pathIDs = helper.getPathsIDs(trip.id);
            JSONArray pathMetaData = new JSONArray();
            for (Long pathId : pathIDs) {
                Path path = helper.getPath(pathId);
                File pathFile = path.getFile();

                ZipEntry pathEntry = new ZipEntry("Paths/"+pathFile.getName());
                zipOutputStream.putNextEntry(pathEntry);
                RandomAccessFile randomAccessFile = new RandomAccessFile(pathFile, "r");
                byte[] bytes = new byte[(int) randomAccessFile.length()];
                randomAccessFile.readFully(bytes);
                zipOutputStream.write(bytes);

                pathMetaData.put(path.toJSONObject(geocoder));
            }
            ZipEntry pathMetaDataEntry = new ZipEntry(Constants.PATH_META);
            zipOutputStream.putNextEntry(pathMetaDataEntry);
            zipOutputStream.write(pathMetaData.toString().getBytes());

            // Media metadata preparing and media and media metadata writing to zip
            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip.id,null,
                    " AND " + LocationDbOpenHelper.COLUMN_SHARE_OPTION +"!=\""+Constants.ONLY_ME+"\"");
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
