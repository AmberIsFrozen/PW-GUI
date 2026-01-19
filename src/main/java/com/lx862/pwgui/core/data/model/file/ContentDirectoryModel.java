package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;

public class ContentDirectoryModel extends DirectoryModel {
    private static final HashMap<String, String> directoryToNameMap = new HashMap<>();

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
    public String getDisplayName() {
        String fileName = path.toFile().getName();
        return directoryToNameMap.getOrDefault(fileName, fileName);
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }

    @Override
    public Icon getIcon() {
        String fileName = path.toFile().getName();
        Icon icon = iconFor(fileName);
        return icon == null ? super.getIcon() : icon;
    }

    private static Icon iconFor(String contentType) {
        switch(contentType) {
            case "mods" -> {
                return new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/mime/mods.png"), 16));
            }
            case "resourcepacks", "plugins" -> {
                return null;
            }
            case "shaderpacks" -> {
                return new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/mime/shaderpacks.png"), 16));
            }
        }
        return null;
    }
}