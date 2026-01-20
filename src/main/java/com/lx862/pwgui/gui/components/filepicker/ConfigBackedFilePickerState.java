package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.PWGUI;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigBackedFilePickerState implements SystemFileChooser.StateStore {
    @Override
    public String get(String key, String def) {
        Path path = PWGUI.getConfig().fileChooserLastPath.get(key);
        return path == null ? def : path.toString();
    }

    @Override
    public void put(String key, String value) {
        if(value != null) {
            PWGUI.getConfig().fileChooserLastPath.put(key, Path.of(value));
        } else {
            PWGUI.getConfig().fileChooserLastPath.remove(key);
        }
        try {
            PWGUI.getConfig().write("Save last file picker path.");
        } catch (IOException e) {
            PWGUI.LOGGER.exception(e);
        }
    }
}
