package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class CurseForgeModpackFilter extends FileFilter implements NativeFileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getName().endsWith(".zip") || file.getName().equals("manifest.json");
    }

    @Override
    public String getDescription() {
        return "CurseForge Modpack (.zip / manifest.json)";
    }

    @Override
    public SystemFileChooser.FileFilter getNativeFileFilter() {
        return nativeExtensionFilter("zip", "json");
    }
}
