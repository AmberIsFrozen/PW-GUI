package com.lx862.pwgui.core.data.model.file;

import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.pwcore.PackwizMetaFile;

import java.io.File;

public class PackMetadataFileModel extends PlainTextFileModel {
    private final PackwizMetaFile packwizMetaFile;

    public PackMetadataFileModel(File file) {
        super(file);
        packwizMetaFile = new PackwizMetaFile(file.toPath());
    }

    @Override
    public String getDisplayName() {
        return Config.getInstance().showMetaFileName.getValue() ? name : packwizMetaFile.name;
    }

    @Override
    public boolean isUserFriendlyName() {
        return !Config.getInstance().showMetaFileName.getValue();
    }

    public PackwizMetaFile getPackMetadata() {
        return packwizMetaFile;
    }
}