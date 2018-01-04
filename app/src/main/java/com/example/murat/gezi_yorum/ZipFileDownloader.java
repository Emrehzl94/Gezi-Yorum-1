package com.example.murat.gezi_yorum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Downloads and imports ZipFiles to database
 */

public class ZipFileDownloader extends Service {
    private ZipFile zipFile;
    private URL url;
    private LocationDbOpenHelper helper;

    private int notificationId = 1000;

    NotificationManager manager;
    private Notification.Builder not;
    private User user;
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
            Toast.makeText(this,  getString(R.string.internet_warning),
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }
        //noinspection ConstantConditions
        try {
            url = new URL(extras.getString("url"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        user = new User(getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE));
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Dosya indiriliyor.").
                setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            not.setChannelId(Constants.CH1);
        }
        startForeground(notificationId, not.build());

        helper = new LocationDbOpenHelper(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                download();
                importZip();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }).start();

        return START_STICKY;
    }
    private void download(){
        try {
            Scanner scanner = new Scanner(url.openStream());
            String fileLink = scanner.next();
            URL website = new URL(Constants.ROOT+fileLink);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(getFilesDir()+"/trip.zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.app_name));
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(getString(R.string.unsuccessful_download));
            builder.setChannelId(Constants.CH1);
            manager.notify(0, builder.build());
            stopSelf();
        }
    }
    private void importZip(){
        try {
            zipFile = new ZipFile(getFilesDir()+"/trip.zip");

            ZipEntry tripMetaEntry = zipFile.getEntry(Constants.TRIP_META);
            JSONObject tripMeta = new JSONObject(entryToString(zipFile, tripMetaEntry));
            long trip_id = helper.importTrip(tripMeta.getLong("startdate"), tripMeta.getLong("finishdate"), tripMeta.getString("name"), user.username);

            ZipEntry mediaMetaEntry = zipFile.getEntry(Constants.MEDIA_META);
            JSONArray mediaMeta = new JSONArray(entryToString(zipFile, mediaMetaEntry));
            for (int i = 0; i< mediaMeta.length() ; i++){
                JSONObject mediaInfo = mediaMeta.getJSONObject(i);
                ZipEntry mediaEntry = zipFile.getEntry("Media/"+mediaInfo.getString("path"));
                String filename = extractEntry(zipFile, mediaEntry, MediaFile.getExtension(mediaInfo.getString("type")));
                MediaFile mediaFile = new MediaFile(mediaInfo, filename, trip_id);
                mediaFile.generateThumbNail(this);
                helper.insertMediaFile(mediaFile);
            }
            ZipEntry pathMetaEntry = zipFile.getEntry(Constants.PATH_META);
            JSONArray pathMeta = new JSONArray(entryToString(zipFile, pathMetaEntry));
            for (int i = 0; i< pathMeta.length() ; i++){
                JSONObject pathInfo = pathMeta.getJSONObject(i);
                //Import path to database
                ZipEntry pathEntry = zipFile.getEntry("Paths/"+pathInfo.getString("file"));
                String filename = extractEntry(zipFile, pathEntry, "csv");
                helper.importPath(trip_id, pathInfo.getLong("start_date"), pathInfo.getLong("finish_date"),
                        filename ,pathInfo.getString("type"));
            }

        } catch (IOException|JSONException ex) {
            ex.printStackTrace();
        }
    }
    private String entryToString(ZipFile file, ZipEntry entry){
        String res = "";
        try {
            Scanner scanner = new Scanner(file.getInputStream(entry));
            StringBuilder builder = new StringBuilder();
            while(scanner.hasNext()){
                builder.append(scanner.nextLine());
            }
            res = builder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return res;
    }
    private String extractEntry(ZipFile file, ZipEntry entry, String extension){
        String filename = System.currentTimeMillis()+"."+extension;
        FileOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[1048576];
            File dir = new File(getFilesDir()+"/Imports/");
            File exfile = new File(getFilesDir()+"/Imports/"+filename);
            if(!exfile.exists()){
                dir.mkdirs();
                exfile.createNewFile();
            }
            ous = new FileOutputStream(exfile);
            ios = file.getInputStream(entry);
            int read;
            int written = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
                written += read;
            }
            filename = exfile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ous != null) ous.close();
                if (ios != null) ios.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filename;
    }
}
