package com.lx862.pwgui.support.packwiz;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.IOException;
import java.nio.file.Path;

public abstract class TomlFile extends WritableFile {
    protected final Toml toml;

    public TomlFile(Path path, Toml toml) {
        super(path);
        this.toml = new Toml().read(toml);
    }

    public Toml getToml() {
        return this.toml;
    }

    protected void writeToFilesystem(Object o) throws IOException {
        new TomlWriter().write(o, getPath().toFile());
    }
}
