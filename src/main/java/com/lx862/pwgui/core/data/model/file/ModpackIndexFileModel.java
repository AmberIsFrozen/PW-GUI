package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;

public class ModpackIndexFileModel extends PlainTextFileModel {
    public ModpackIndexFileModel(File file) {
        super(file);
    }

    @Override
    public String getDisplayName() {
        return "Packwiz Index";
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/packwiz.png"), 18));
    }
}