package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;

public class GitDirectoryModel extends DirectoryModel {
    public GitDirectoryModel(File file) {
        super(file);
    }

    @Override
    public String getDisplayName() {
        return "Git Repository";
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/git.png"), 18));
    }
}
