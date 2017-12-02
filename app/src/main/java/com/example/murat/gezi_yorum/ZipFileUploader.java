package com.example.murat.gezi_yorum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Utils.LocationCSVHandler;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MultipartUtility;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates ZipFiles and read data from them
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
        if(cm.getActiveNetworkInfo() == null){
            Toast.makeText(this, "Etkin internet bağlantısı bulunmamaktadır. Lütfen daha sonra tekrar deneyin.",
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }
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
    private void upload(){
        not.setContentTitle("Dosya yükleniyor...");
        manager.notify(notificationId,not.build());
        String URL = "http://163.172.176.169:8080/Geziyorum/uploadFile";
         //String URL = "http://trendbul.yavuzmacit.com/fileupload.php";
        try {
            MultipartUtility multipart = new MultipartUtility(URL,"UTF-8", false);
            multipart.addFormField("name",zipFile.getName());
            multipart.addFilePart("file",zipFile);
            List<String> messages = multipart.finish();
            for(String message : messages){
                Log.w("Respons", "upload: " + message);
            }
            not.setContentTitle("Dosya başarıyla yüklendi.");
            manager.notify(notificationId,not.build());
        } catch (Exception e) {
            e.printStackTrace();
            not.setContentTitle("Dosya yüklemesi sırasında hata meydana geldi..");
            manager.notify(notificationId,not.build());
        }

    }
    private void create(){
        try {
            if(!zipFile.exists()){
                zipFile.createNewFile();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            ArrayList<Long> pathIDs = helper.getPathsIDs(trip_id);
            JSONArray pathMetaData = new JSONArray();
            for (Long pathId : pathIDs) {
                File pathFile = LocationCSVHandler.getRouteFilePath(trip_id, pathId, getApplicationContext());
                ZipEntry pathEntry = new ZipEntry("Paths/"+pathFile.getName());
                zipOutputStream.putNextEntry(pathEntry);
                RandomAccessFile randomAccessFile = new RandomAccessFile(pathFile, "r");
                byte[] bytes = new byte[(int) randomAccessFile.length()];
                randomAccessFile.readFully(bytes);
                zipOutputStream.write(bytes);
                pathMetaData.put(helper.getPathInfo(pathId));
            }

            ZipEntry pathMetaDataEntry = new ZipEntry("path_metadata.JSON");
            zipOutputStream.putNextEntry(pathMetaDataEntry);
            zipOutputStream.write(pathMetaData.toString().getBytes());

            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip_id,null,null);
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
            ZipEntry mediaMetaDataEntry = new ZipEntry("media_metadata.JSON");
            zipOutputStream.putNextEntry(mediaMetaDataEntry);
            zipOutputStream.write(mediaMetaData.toString().getBytes());
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeByteArrayOriginal(File file, OutputStream zipOutputStream){
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[1048576];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            int written = 0;
            Integer length = Integer.parseInt(Long.valueOf(file.length()).toString());
            while ((read = ios.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, read);
                not.setProgress(100, 100*written/length, false);
                manager.notify(notificationId, not.build());
                written += read;
            }
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
    }
}
