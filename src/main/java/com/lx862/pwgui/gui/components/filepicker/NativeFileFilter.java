package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

public interface NativeFileFilter {
    SystemFileChooser.FileFilter getNativeFileFilter();

    String getDescription();

    default SystemFileChooser.FileFilter nativeExtensionFilter(String... extensionName) {
        return new SystemFileChooser.FileNameExtensionFilter(getDescription(), extensionName);
    }
}
