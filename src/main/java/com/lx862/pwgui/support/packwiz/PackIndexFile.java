package com.lx862.pwgui.support.packwiz;

import com.lx862.pwgui.util.Util;
import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class PackIndexFile extends TomlFile {
    private final List<FileEntry> fileEntries;
    private final String hashFormat;

    public PackIndexFile(Path modpackRoot, Path path) {
        this(modpackRoot, path, new Toml().read(path.toFile()));
    }

    public PackIndexFile(Path modpackRoot, Path path, Toml toml) {
        super(path, toml);

        this.fileEntries = new ArrayList<>();
        this.hashFormat = toml.getString("hash-format");

        List<Toml> entries = toml.getTables("files");
        if(entries == null) return;
        for(Toml entryToml : entries) {
            fileEntries.add(new FileEntry(modpackRoot, entryToml, this.hashFormat, path));
        }
    }

    @Override
    public void write(String reason) throws IOException {
        Map<String, Object> map = toml.toMap();
        map.put("hash-format", this.hashFormat);

        List<Map<String, Object>> entries = new ArrayList<>();
        for(FileEntry entry : fileEntries) {
            Map<String, Object> entryMap = new HashMap<>();
            entryMap.put("file", entry.file);
            entryMap.put("hash", entry.hash);
            if(entry.alias != null) entryMap.put("alias", entry.alias);
            if(!this.hashFormat.equals(entry.hashFormat)) entryMap.put("hash-format", entry.hashFormat);
            if(entry.metafile) entryMap.put("metafile", true);
            if(entry.preserve) entryMap.put("preserve", true);

            entries.add(entryMap);
        }
        map.put("files", entries);
        writeToFilesystem(map);
        super.write(reason);
    }

    public List<FileEntry> getFileEntries() {
        return new ArrayList<>(this.fileEntries);
    }

    /**
     * Returns whether the specified file is marked as a metafile in the packwiz index file.
     */
    public boolean isMetafile(Path path) {
        FileEntry fileEntry = getEntryByPath(path);
        if(fileEntry == null) return false;

        return fileEntry.metafile;
    }

    public FileEntry getEntryByPath(Path path) {
        for(FileEntry entry : fileEntries) {
            if(entry.path.equals(path)) return entry;
        }
        return null;
    }

    public void updateEntry(FileEntry newFileEntry) {
        FileEntry originalEntry = getEntryByPath(newFileEntry.path);
        if(originalEntry == null) throw new IllegalArgumentException(String.format("Index file does not contain entry: %s", newFileEntry.path));
        int index = fileEntries.indexOf(originalEntry);
        fileEntries.set(index, newFileEntry);
    }

    public static class FileEntry {
        public final Path path;
        public final String file;
        public final String hash;
        public final String hashFormat;
        public final String alias;
        public boolean metafile;
        public boolean preserve;

        public FileEntry(Path modpackRoot, Toml toml, String indexHashFormat, Path indexFilePath) {
            this.file = toml.getString("file");
            this.path = indexFilePath.getParent().resolve(this.file);
            if(!Util.withinDirectory(modpackRoot, this.path)) throw new IllegalStateException(String.format("Referenced file %s must not be outside the modpack folder!", this.file));
            this.hash = toml.getString("hash");
            this.alias = toml.getString("alias");
            this.hashFormat = toml.getString("hash-format") == null ? indexHashFormat : toml.getString("hash-format");
            this.preserve = toml.getBoolean("preserve", false);
            this.metafile = toml.getBoolean("metafile", false);
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;

            if(o instanceof FileEntry fileEntry) {
                return fileEntry.path.equals(path);
            } else {
                return false;
            }
        }
    }
}