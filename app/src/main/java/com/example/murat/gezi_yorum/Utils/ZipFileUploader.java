package com.example.murat.gezi_yorum.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.R;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates ZipFiles and read data from them
 */

public class ZipFileUploader extends AsyncTask<String,Integer,String> {
    private File zipFile;
    private ArrayList<File> pathFiles;
    private long trip_id;
    private LocationDbOpenHelper helper;
    private String progressMessage = "";
    private ProgressDialog progressDialog;

    public ZipFileUploader(long trip_id, Context context){
        this.trip_id = trip_id;
        this.zipFile = new File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)) + "/trip_"+trip_id+".zip");
        this.pathFiles = new ArrayList<>();
        for(long path_id : new LocationDbOpenHelper(context).getPathsIDs(trip_id)){
            pathFiles.add(LocationCSVHandler.getRouteFilePath(trip_id,path_id,context));
        }

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Dosyalar Hazırlanıyor...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        helper = new LocationDbOpenHelper(context);
    }

    private void createAndUploadZipFile(){
        create();
        upload();
    }
    private void upload(){
        progressMessage = "Dosya yükleniyor...";
        String URL = "http://163.172.176.169:8080/Geziyorum/uploadFile";
         //String URL = "http://trendbul.yavuzmacit.com/fileupload.php";
        try {
            MultipartUtility multipart = new MultipartUtility(URL,"UTF-8");
            multipart.addFormField("name",zipFile.getName());
            multipart.addFilePart("file",zipFile);
            String response = multipart.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void create(){
        try {
            if(!zipFile.exists()){
                zipFile.createNewFile();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            //TODO: pathMetaData will add to zip in right format.
            JSONArray pathMetaData = new JSONArray();
            for (File pathFile : pathFiles) {
                ZipEntry pathEntry = new ZipEntry("Paths/"+pathFile.getName());
                zipOutputStream.putNextEntry(pathEntry);
                RandomAccessFile randomAccessFile = new RandomAccessFile(pathFile, "r");
                byte[] bytes = new byte[(int) randomAccessFile.length()];
                randomAccessFile.readFully(bytes);
                zipOutputStream.write(bytes);
            }

            ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip_id,null,null);
            JSONArray mediaMetaData = new JSONArray();
            int fileCount = 0;
            for (MediaFile file: mediaFiles){
                progressMessage = ""+(fileCount++)+"/"+mediaFiles.size();
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
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            int written = 0;
            Integer length = Integer.parseInt(Long.valueOf(file.length()).toString());
            while ((read = ios.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, read);
                publishProgress(100*written/length);
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
        progressDialog.setMessage(progressMessage);
    }
}
