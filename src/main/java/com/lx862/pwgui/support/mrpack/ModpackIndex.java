package com.lx862.pwgui.support.mrpack;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModpackIndex {
    public static final String FILE_NAME = "modrinth.index.json";
    public int formatVersion;
    public String game;
    public String versionId;
    public String name;
    public String summary;
    public List<ModpackFileEntry> files;
    public HashMap<String, String> dependencies;

    public static final String DEP_MINECRAFT = "minecraft";
    public static final String DEP_FORGE = "forge";
    public static final String DEP_NEOFORGE = "neoforge";
    public static final String DEP_FABRIC = "fabric-loader";
    public static final String DEP_QUILT = "quilt-loader";

    public void validateSpec() {
        if(formatVersion != 1) throw new IllegalArgumentException("formatVersion must be 1!");
        if(!Objects.equals(game, "minecraft")) throw new IllegalArgumentException("game must be \"minecraft\"");
        if(versionId == null) throw new IllegalArgumentException("versionId is not supplied!");
        if(name == null) throw new IllegalArgumentException("name is not supplied!");
        if(files == null) throw new IllegalArgumentException("files is not supplied!");
        if(dependencies == null) throw new IllegalArgumentException("dependencies is not supplied!");
        files.forEach(ModpackFileEntry::validate);
    }
}
