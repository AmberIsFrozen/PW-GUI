package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Locale;

public class PackwizExecutableFileFilter extends FileFilter implements NativeFileFilter {
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().equals("packwiz") || f.getName().equals("packwiz.exe");
    }

    @Override
    public String getDescription() {
        return "Packwiz Executable (packwiz/packwiz.exe)";
    }

    @Override
    public SystemFileChooser.FileFilter getNativeFilePicker() {
        // Require .exe on Windows
        // Unfortunately we can't specify an empty extension, so have to rely on the default "All Files" for macOS/linux executable, which doesn't have file extensions :(
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if(os.equals("windows")) {
            return new SystemFileChooser.FileNameExtensionFilter(getDescription(), "exe", "");
        }
        return null;
    }
}
