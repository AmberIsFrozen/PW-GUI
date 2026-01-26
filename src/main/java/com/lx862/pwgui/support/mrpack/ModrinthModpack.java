package com.lx862.pwgui.support.mrpack;

import com.google.gson.Gson;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModrinthModpack {
    private final File file;
    public final ModpackIndex index;

    public ModrinthModpack(Path zipPath) throws IOException {
        if(!Files.exists(zipPath)) throw new FileNotFoundException();

        File zipFile = zipPath.toFile();
        this.file = zipFile;
        if(!ZipUtil.containsEntry(zipFile, ModpackIndex.FILE_NAME)) throw new IllegalArgumentException(String.format("Modpack file does not contain %s!", ModpackIndex.FILE_NAME));
        index = new Gson().fromJson(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(ZipUtil.unpackEntry(zipFile, ModpackIndex.FILE_NAME))).toString(), ModpackIndex.class);
        index.validateSpec();
    }

    public File getFile() {
        return this.file;
    }
}
