package com.example.murat.gezi_yorum.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

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

public class ZipFileUploader extends AsyncTask<String,Integer,String> {
    private File zipFile;
    private File pathFile;
    private long trip_id;
    private LocationDbOpenHelper helper;
    private ProgressDialog progressDialog;

    public ZipFileUploader(long trip_id, Context context){
        this.trip_id = trip_id;
        this.zipFile = new File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)) + "/trip_"+trip_id+".zip");
        this.pathFile = LocationCSVHandler.getRouteFilePath(trip_id, context);

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Dosyalar Hazırlanıyor...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        helper = new LocationDbOpenHelper(context);
    }

    private void createAndUploadZipFile(){
        try {
            if(!zipFile.exists()){
                zipFile.createNewFile();
            }
            publishProgress(25);

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry pathEntry = new ZipEntry(pathFile.getName());
            zipOutputStream.putNextEntry(pathEntry);

            RandomAccessFile randomAccessFile= new RandomAccessFile(pathFile,"r");
            byte[] bytes = new byte[(int)randomAccessFile.length()];
            randomAccessFile.readFully(bytes);
            zipOutputStream.write(bytes);

            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip_id,null,null);
            JSONArray mediaMetaData = new JSONArray();
            int i = 0;
            for (MediaFile file: mediaFiles){
                publishProgress(75*(i++)/mediaFiles.size() + 25);
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
    }

    @Override
    protected String doInBackground(String... strings) {
        createAndUploadZipFile();
        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String aString) {
        super.onPostExecute(aString);
        progressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }
}
