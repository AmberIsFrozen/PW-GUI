package com.lx862.pwgui.core;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;

import java.io.InputStream;
import java.io.InputStreamReader;

public class BuildMetadata {
    public String name;
    public String version;
    public String author;

    public static BuildMetadata INSTANCE;

    public static void init() {
        try(InputStream is = Util.getAssets("/build.json")) {
            if(is != null) {
                INSTANCE = new Gson().fromJson(new JsonReader(new InputStreamReader(is)), BuildMetadata.class);
            } else {
                INSTANCE = new BuildMetadata();
            }
        } catch (Exception e) {
            INSTANCE = new BuildMetadata();
            PWGUI.LOGGER.error("Failed to read build metadata!", e);
        }
    }
}
