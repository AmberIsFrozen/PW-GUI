package com.lx862.pwgui.core.data.model.file;

import java.io.File;

public class MinecraftOptionsFileModel extends PlainTextFileModel {
    public MinecraftOptionsFileModel(File file) {
        super(file);
    }

    @Override
    public String getCustomName() {
        return "Minecraft Options File";
    }
}
