package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.io.File;

public class ModrinthPackFileModel extends GenericFileModel {
    public ModrinthPackFileModel(File file) {
        super(file);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/services/modrinth.png"), 18));
    }
}
