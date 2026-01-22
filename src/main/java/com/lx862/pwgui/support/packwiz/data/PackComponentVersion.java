package com.lx862.pwgui.support.packwiz.data;

public class PackComponentVersion {
    private final PackComponent component;
    private final String version;

    public PackComponentVersion(PackComponent packComponent, String version) {
        this.component = packComponent;
        this.version = version;
    }

    public PackComponent getComponent() {
        return this.component;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(o instanceof PackComponentVersion other) {
            return other.version.equals(this.version) && other.component.equals(this.component);
        } else {
            return false;
        }
    }
}
