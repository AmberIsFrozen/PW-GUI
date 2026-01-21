package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigBackedFilePickerState implements SystemFileChooser.StateStore {
    @Override
    public String get(String key, String def) {
        Path path = Config.getInstance().fileChooserLastPath.get(key);
        return path == null ? def : path.toString();
    }

    @Override
    public void put(String key, String value) {
        if(value != null) {
            Config.getInstance().fileChooserLastPath.put(key, Path.of(value));
        } else {
            Config.getInstance().fileChooserLastPath.remove(key);
        }
        try {
            Config.getInstance().write("Save last file picker path.");
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
        }
    }
}
