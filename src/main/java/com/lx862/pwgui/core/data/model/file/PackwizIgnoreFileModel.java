package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class PackwizIgnoreFileModel extends PlainTextFileModel {
    public PackwizIgnoreFileModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "Packwiz Ignore list";
    }
}
