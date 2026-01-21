package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateMissingDirectoryAction extends AbstractAction {
    private final Window parent;
    private final Path root;
    private final String dirName;

    public CreateMissingDirectoryAction(Window parent, Path root, String dirName, String menuName) {
        super(menuName);
        this.parent = parent;
        this.root = root;
        this.dirName = dirName;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Path finalPath = root.resolve(dirName);
        if(finalPath.toFile().exists()) {
            JOptionPane.showMessageDialog(parent, String.format("Folder \"%s\" already exists!", dirName), Util.withTitlePrefix("Folder Already Exists!"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            try {
                Files.createDirectory(finalPath);
                JOptionPane.showMessageDialog(parent, String.format("Created folder \"%s\"!", dirName), Util.withTitlePrefix("Folder Created!"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                PWGUI.LOGGER.error("Failed to create folder!", e);
                JOptionPane.showMessageDialog(parent, String.format("Failed to create folder \"%s\", see program logs for detail!", dirName), Util.withTitlePrefix("Failed to Create Folder"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
