package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;

public class ContentDirectoryModel extends DirectoryModel {
    private static final HashMap<String, NameIcon> directoryToNameMap = new HashMap<>();

    static {
        directoryToNameMap.put("mods", new NameIcon("Mods", new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/mime/mods.png"), 16))));
        directoryToNameMap.put("resourcepacks", new NameIcon("Resource Packs", null));
        directoryToNameMap.put("shaderpacks", new NameIcon("Shader Packs", new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/mime/shaderpacks.png"), 16))));
        directoryToNameMap.put("plugins", new NameIcon("Plugins", null));
    }

    public ContentDirectoryModel(File file) {
        super(file);
    }

    @Override
    public String getDisplayName() {
        String fileName = path.toFile().getName();
        return directoryToNameMap.getOrDefault(fileName, new NameIcon(fileName, null)).name();
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }

    @Override
    public Icon getIcon() {
        String fileName = path.toFile().getName();
        Icon icon = directoryToNameMap.getOrDefault(fileName, new NameIcon(fileName, null)).icon();
        return icon == null ? super.getIcon() : icon;
    }

    record NameIcon(String name, Icon icon) {}
}