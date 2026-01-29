package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class LicenseFileModel extends PlainTextFileModel {
    public LicenseFileModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "License";
    }

    @Override
    public boolean isUserFriendlyName() {
        return true;
    }
}
