package com.lx862.pwgui.core.data.model.file;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

public abstract class FileSystemEntityModel {
    public final String name;
    public final Path path;

    public FileSystemEntityModel(File file) {
        this.path = file.toPath();
        this.name = file.getName();
    }

    public final String getDisplayName() {
        String customName = getCustomName();
        return customName == null ? name : customName;
    }

    protected String getCustomName() {
        return null;
    }

    public boolean isUserFriendlyName() {
        return getCustomName() != null;
    }
}
