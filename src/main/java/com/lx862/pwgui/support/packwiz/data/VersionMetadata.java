package com.lx862.pwgui.support.packwiz.data;

/* This class represents version metadata, such as Minecraft version, Fabric version, Modrinth mod version, etc.*/
public class VersionMetadata {
    private final String minecraftVersion;
    private final String versionName;
    private final State state;

    public VersionMetadata(String minecraftVersion, String versionName, State state) {
        this.minecraftVersion = minecraftVersion;
        this.versionName = versionName;
        this.state = state;
    }

    public String getVersionName() {
        return this.versionName;
    }

    /** Returns either the Minecraft version this version is designed for, or null if the version is agnostic to MC versions. */
    public String getAccompaniedMinecraftVersion() {
        return this.minecraftVersion;
    }

    public State getState() {
        return this.state;
    }

    @Override
    public String toString() {
        return this.versionName;
    }

    public enum State {
        RELEASE,
        BETA,
        ALPHA
    }
}
