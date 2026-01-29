package com.lx862.pwgui.util;

import com.lx862.pwgui.PWGUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkHelper {
    public static String getRequestToString(String url) throws IOException {
        HttpURLConnection conn = getConnection(url, "GET", "application/json");
        int responseCode = conn.getResponseCode();
        PWGUI.LOGGER.info("Got HTTP {} for {}", responseCode, url);
        return readString(conn.getInputStream());
    }

    public static HttpURLConnection postRequest(String url, byte[] bodyData) throws IOException {
        HttpURLConnection connection = getConnection(url, "POST", "application/json");
        if(bodyData != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(bodyData);
        }
        return connection;
    }

    public static HttpURLConnection getConnection(String url, String requestMethod, String contentType) throws IOException {
        try {
            URL constructedUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) constructedUrl.openConnection();
            conn.setRequestMethod(requestMethod);
            if(contentType != null) conn.setRequestProperty("Content-Type", contentType);
            PWGUI.LOGGER.info("{} request to {}", requestMethod, url);
            return conn;
        } catch (MalformedURLException ignored) { // We have faith in our URL (tm)
            throw new RuntimeException();
        }
    }

    public static String readString(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder resp = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            resp.append(line).append("\n");
        }
        br.close();
        return resp.toString();
    }
}
