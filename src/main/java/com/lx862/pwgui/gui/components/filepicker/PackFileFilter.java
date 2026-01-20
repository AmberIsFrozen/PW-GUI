package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.nio.file.Files;

public class PackFileFilter extends FileFilter implements NativeFileFilter {
    @Override
    public boolean accept(File file) {
        if(file.isDirectory()) return true;
        if(file.getName().endsWith(".pw.toml")) return false;

        // index.toml, but we also have pack.toml in the same directory, so we hide it to avoid confusion, as it's very likely it's pack.toml in this case.
        if(file.getName().equals("index.toml") && Files.exists(file.toPath().getParent().resolve("pack.toml"))) return false;
        return file.getName().endsWith(".toml"); // Packwiz supports filename other than pack.toml, so we don't only whitelist pack.toml.
    }

    @Override
    public String getDescription() {
        return "Packwiz Pack File (pack.toml by default)";
    }

    @Override
    public SystemFileChooser.FileFilter getNativeFilePicker() {
        return new SystemFileChooser.FileNameExtensionFilter(getDescription(), "toml");
    }
}
