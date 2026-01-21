package com.lx862.pwgui.pwcore;

import com.lx862.pwgui.PWGUI;

import java.io.IOException;
import java.nio.file.Path;

public abstract class WritableFile {
    private final Path path;

    public WritableFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    public void write(String reason) throws IOException {
        PWGUI.LOGGER.info("Writing file to \"{}\" due to \"{}\"", path.toString(), reason);
    }
}
