package com.example.murat.gezi_yorum.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Handles HttpRequests for JSON implementation. Npt now but in future.
 */

public class URLRequestHandler {
    private String response;
    private String URL;
    private byte[] bytedata;
    public URLRequestHandler(String data,String url){
        bytedata = data.getBytes();
        this.URL = url;
    }
    public boolean getResponseMessage(){
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(bytedata.length));
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(bytedata);
            try {
                if (connection.getResponseCode() != 200) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());

            }
            this.response = builder.toString();
            connection.disconnect();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public String getResponse() {
        return response;
    }
}
