package com.lx862.pwgui.core.data.model.file;

import javax.swing.*;
import java.io.File;

public class GenericFileModel extends FileSystemEntityModel {
    public final long fileSize;
    public final long lastModified;

    public GenericFileModel(File file) {
        super(file);
        this.fileSize = file.length();
        this.lastModified = file.lastModified();
    }
}
