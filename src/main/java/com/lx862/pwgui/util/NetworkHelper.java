package com.lx862.pwgui.util;

import com.lx862.pwgui.PWGUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelper {
    public static String getFromURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        PWGUI.LOGGER.info("{} request to {}", connection.getRequestMethod(), url);
        int responseCode = connection.getResponseCode();
        PWGUI.LOGGER.info("Got HTTP {} for {}", responseCode, url);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder resp = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            resp.append(line).append("\n");
        }
        br.close();
        return resp.toString();
    }
}
