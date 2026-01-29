package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class GitDirectoryModel extends DirectoryModel {
    public GitDirectoryModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "Git Repository";
    }
}
