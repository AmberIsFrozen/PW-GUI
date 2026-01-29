package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class ModpackIndexFileModel extends PlainTextFileModel {
    public ModpackIndexFileModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "Packwiz Index";
    }
}