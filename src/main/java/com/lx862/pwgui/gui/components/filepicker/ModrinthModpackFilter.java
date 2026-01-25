package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ModrinthModpackFilter extends FileFilter implements NativeFileFilter {
    @Override
    public boolean accept(File f) {
        return f.getName().endsWith(".mrpack");
    }

    @Override
    public String getDescription() {
        return "Modrinth Modpack (.mrpack)";
    }

    @Override
    public SystemFileChooser.FileFilter getNativeFileFilter() {
        return nativeExtensionFilter("mrpack");
    }
}
