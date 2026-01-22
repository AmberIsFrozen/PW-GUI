package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.filepicker.PackwizExecutableFileFilter;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class LocatePackwizAction extends AbstractAction {
    private final Window parent;
    private final Runnable finishCallback;

    public LocatePackwizAction(String title, Window parent, Runnable finishCallback) {
        super(title);
        this.parent = parent;
        this.finishCallback = finishCallback;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        KFileChooser fileChooser = new KFileChooser("locate-pw");
        fileChooser.setFileFilter(new PackwizExecutableFileFilter());

        if(fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if(!selectedFile.canExecute()) {
                try {
                    Set<PosixFilePermission> perms = Files.getPosixFilePermissions(selectedFile.toPath(), LinkOption.NOFOLLOW_LINKS);
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(selectedFile.toPath(), perms);
                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(parent, "The selected file is not executable!\nConsider adding the executable (x) permission to the file.", Util.withTitlePrefix("File Not Executable!"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Config.getInstance().packwizExecutablePath.setValue(selectedFile.toPath());
            String newProbedPath = PackwizExecutable.INSTANCE.probe(null);
            if(newProbedPath == null) {
                JOptionPane.showMessageDialog(parent, "The selected executable is not valid!\nPlease confirm that you can run the executable?", Util.withTitlePrefix("Invalid Executable"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Config.getInstance().write("Update packwiz executable path");
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(parent, String.format("Failed to write configuration file:\n%s\nSee program logs for detail!", e.getMessage()), Util.withTitlePrefix("Failed to Write Config"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            finishCallback.run();
        }
    }
}
