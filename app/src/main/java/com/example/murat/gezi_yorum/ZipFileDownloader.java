package com.example.murat.gezi_yorum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Downloads and imports ZipFiles to database
 */

public class ZipFileDownloader extends Service {
    private ZipFile zipFile;
    private String url;
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
            Toast.makeText(this, "Etkin internet bağlantısı bulunmamaktadır. Lütfen daha sonra tekrar deneyin.",
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }
        //noinspection ConstantConditions
        url = extras.getString("url");

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentText("Dosya indiriliyor.").
                setSmallIcon(R.mipmap.ic_launcher).
                setProgress(100,0,false);
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
    }
    private void importZip(){
        try {
            this.zipFile = new ZipFile(new File(url));
            ZipEntry entry = zipFile.getEntry(Constants.MEDIA_META);
            String s = entry.toString();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
