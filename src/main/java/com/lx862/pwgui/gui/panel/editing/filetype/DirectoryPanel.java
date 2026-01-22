package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.DirectoryModel;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class DirectoryPanel extends FileTypePanel {

    public DirectoryPanel(FileEntryPaneContext context, DirectoryModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel fileNameLabel = new JLabel(String.format("Folder name: %s", fileEntry.name));
        fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(fileNameLabel);

        JLabel fileCountLabel = new JLabel(String.format("Files count: %s", fileEntry.path.toFile().list().length));
        fileCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(fileCountLabel);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        KButton openButton = new KButton("Open folder");
        openButton.setMnemonic(KeyEvent.VK_O);
        openButton.addActionListener(actionEvent -> Util.tryOpenFile(fileEntry.path.toFile()));

        actionButtons.add(openButton);

        KButton removeButton = new KButton("Remove folder");
        removeButton.setMnemonic(KeyEvent.VK_R);
        removeButton.addActionListener(actionEvent -> {
            final boolean shouldDelete = JOptionPane.showConfirmDialog(getTopLevelAncestor(), String.format("Are you sure you want to delete \"%s\"?", fileEntry.name), Util.withTitlePrefix("Delete Confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if(shouldDelete) {
                try {
                    FileUtils.deleteDirectory(fileEntry.path.toFile());
                    PWGUI.LOGGER.info("Deleted folder {}", fileEntry.path);
                    PackwizExecutable.INSTANCE.refresh().build().run("Folder deleted by user");
                } catch (IOException e) {
                    PWGUI.LOGGER.error("Failed to deleted folder {} due to {}", fileEntry.path, e.getMessage());
                    JOptionPane.showMessageDialog(getTopLevelAncestor(), String.format("Sorry but we are unable to delete the folder, error as follows: \n%s\nYou might try doing it from an external file manager.", e.getMessage()));
                }
            }
        });
        actionButtons.add(removeButton);

        add(actionButtons);
    }
}
