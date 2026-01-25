package com.lx862.pwgui.gui.components.kui;

import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.components.filepicker.ConfigBackedFilePickerState;
import com.lx862.pwgui.gui.components.filepicker.NativeFileFilter;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class KFileChooser extends SystemFileChooser {
    private static final ConfigBackedFilePickerState FILE_PICKER_STATE = new ConfigBackedFilePickerState();

    @SuppressWarnings("unused")
    public KFileChooser() {
        this(null);
    }

    public KFileChooser(String context) {
        this(context, null);
    }

    public KFileChooser(String context, Path defaultPath) {
        SystemFileChooser.setStateStore(FILE_PICKER_STATE);
        if(context != null) {
            setStateStoreID(context);
            if(defaultPath != null && FILE_PICKER_STATE.get(context + "." + StateStore.KEY_CURRENT_DIRECTORY, null) == null) {
                setCurrentDirectory(defaultPath.toFile());
            }
        }
    }

    /** Open the Save As dialog. User will be prompted if the file would get overwritten */
    public int openSaveAsDialog(Component component) {
        int showDialogResult = showSaveDialog(component);
        if(showDialogResult == APPROVE_OPTION && !isNativeFilePicker()) {
            if(Files.exists(getSelectedFile().toPath())) { // Prompt for overwrite
                int replaceResult = JOptionPane.showConfirmDialog(component, String.format("File \"%s\" already exist,\nAre you sure you want to replace the file?", getSelectedFile().getName()), Util.withTitlePrefix("Replace File?"), JOptionPane.YES_NO_OPTION);
                if(replaceResult != JOptionPane.YES_OPTION) {
                    return openSaveAsDialog(component); // Ask again
                }
            }
        }
        return showDialogResult;
    }

    /** Open the Save As dialog. User will be prompted if the folder is not empty */
    public int openSaveDirectoryDialog(Component component) {
        setApproveCallback((selected, ctx) -> {
            try(Stream<Path> files = Files.list(selected[0].toPath())) {
                if(files.findAny().isPresent()){
                    ctx.showMessageDialog(JOptionPane.INFORMATION_MESSAGE, "Folder not empty", "The selected folder is not empty, please create an empty folder for the modpack to be stored in.", JOptionPane.OK_OPTION);
                    return CANCEL_OPTION;
                }
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
            }
            return APPROVE_OPTION;
        });

        return showSaveDialog(component);
    }

    public void setFileFilter(NativeFileFilter fileFilter) {
        FileFilter newFileFilter = fileFilter.getNativeFileFilter();
        if(newFileFilter != null) {
            super.setFileFilter(fileFilter.getNativeFileFilter());
        }
    }

    public void setSaveAsFileName(String fileName) {
        File originalRootDirectory = getCurrentDirectory().getAbsoluteFile();
        // setSelectedFile would also change current directory to the file's parent directory
        // We don't want that as we only care about the file name, so let's resolve the file path to be underneath our current dir.
        super.setSelectedFile(originalRootDirectory.toPath().resolve(fileName).toFile());
        setCurrentDirectory(originalRootDirectory);
    }

    private static boolean isNativeFilePicker() {
        return FlatSystemProperties.getBoolean(FlatSystemProperties.USE_SYSTEM_FILE_CHOOSER, true);
    }
}
