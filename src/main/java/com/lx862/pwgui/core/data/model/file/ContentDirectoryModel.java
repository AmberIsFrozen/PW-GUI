package com.lx862.pwgui.core.data.model.file;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContentDirectoryModel extends DirectoryModel {
    private static final Map<String, String> directoryToNameMap = new HashMap<>();

    static {
        directoryToNameMap.put("mods", "Mods");
        directoryToNameMap.put("resourcepacks", "Resource Packs");
        directoryToNameMap.put("shaderpacks", "Shader Packs");
        directoryToNameMap.put("plugins", "Plugins");
    }

    public ContentDirectoryModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        String fileName = path.toFile().getName();
        return directoryToNameMap.getOrDefault(fileName, fileName);
    }
}