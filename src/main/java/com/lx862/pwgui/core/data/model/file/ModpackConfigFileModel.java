package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.support.packwiz.PackFile;
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
            PWGUI.LOGGER.error("", e);
        }
    }

    @Override
    public String getCustomName() {
        return "Modpack Config";
    }
}