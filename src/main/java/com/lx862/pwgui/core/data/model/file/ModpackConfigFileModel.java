package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.pwcore.PackFile;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;

public class ModpackConfigFileModel extends PlainTextFileModel {
    public PackFile packFile;

    public ModpackConfigFileModel(File file) {
        super(file);
        try {
            packFile = new PackFile(file.toPath());
        } catch (Exception e) {
            PWGUI.LOGGER.exception(e);
        }
    }

    @Override
    public String getDisplayName() {
        return "Modpack Config";
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/ui/mime/settings.png"), 18));
    }
}