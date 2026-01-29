package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class GitIgnoreFileModel extends PlainTextFileModel {
    public GitIgnoreFileModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "Git Ignore list";
    }
}
