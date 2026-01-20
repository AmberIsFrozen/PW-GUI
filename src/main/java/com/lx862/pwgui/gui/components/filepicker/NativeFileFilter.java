package com.lx862.pwgui.gui.components.filepicker;

import com.formdev.flatlaf.util.SystemFileChooser;

public interface NativeFileFilter {
    SystemFileChooser.FileFilter getNativeFilePicker();
}
