package com.example.murat.gezi_yorum.classes;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates ZipFiles and read data from them
 */

public class ZipFileHandler {
    private File zipFile;
    private File pathFile;
    private long trip_id;
    private LocationDbOpenHelper helper;
    private Context context;

    public ZipFileHandler(long trip_id, Context context){
        this.trip_id = trip_id;
        this.zipFile = new File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)) + "/trip_"+trip_id+".zip");
        this.pathFile = LocationCSVHandler.getRouteFilePath(trip_id, context);
        this.context = context;
        helper = new LocationDbOpenHelper(context);
    }

    public void createAndUploadZipFile(){
        try {
            if(!zipFile.exists()){
                zipFile.createNewFile();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry pathEntry = new ZipEntry(pathFile.getName());
            zipOutputStream.putNextEntry(pathEntry);
            RandomAccessFile randomAccessFile= new RandomAccessFile(pathFile,"r");
            byte[] bytes = new byte[(int)randomAccessFile.length()];
            randomAccessFile.readFully(bytes);
            zipOutputStream.write(bytes);


            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip_id,null,null);
            JSONArray mediaMetaData = new JSONArray();
            for (MediaFile file: mediaFiles){
                ZipEntry mediaFileEntry = new ZipEntry("Media/"+new File(file.path).getName());
                zipOutputStream.putNextEntry(mediaFileEntry);
                zipOutputStream.write(file.getByteArrayOriginal());
                mediaMetaData.put(file.toJSONObject());
            }

            ZipEntry mediaMetaDataEntry = new ZipEntry("media_metadata.JSON");
            zipOutputStream.putNextEntry(mediaMetaDataEntry);
            zipOutputStream.write(mediaMetaData.toString().getBytes());
            zipOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(context,"Gezi "+zipFile.getAbsolutePath()+" konumuna sıkıştırıldı.",Toast.LENGTH_LONG).show();
    }

}
