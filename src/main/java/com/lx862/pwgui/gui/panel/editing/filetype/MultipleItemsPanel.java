package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.DirectoryModel;
import com.lx862.pwgui.core.data.model.file.FileSystemEntityModel;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MultipleItemsPanel extends FileTypePanel {

    public MultipleItemsPanel(FileEntryPaneContext context, List<FileSystemEntityModel> fileEntries) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        int directories = fileEntries.stream().filter(e -> e instanceof DirectoryModel).toList().size();
        int files = fileEntries.stream().filter(e -> e instanceof GenericFileModel).toList().size();

        JLabel summaryLabel = new JLabel(directories == 0 ? String.format("%d files selected", files) : files == 0 ? String.format("%d folders selected", directories) : String.format("%d folder(s) and %d file(s) selected", directories, files));
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(summaryLabel);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        KButton openButton = new KButton("Open All");
        openButton.setMnemonic(KeyEvent.VK_O);
        openButton.addActionListener(actionEvent -> {
            for(FileSystemEntityModel model : fileEntries) {
                Util.tryOpenFile(model.path.toFile());
            }
        });

        actionButtons.add(openButton);

        KButton removeButton = new KButton("Remove All");
        removeButton.setMnemonic(KeyEvent.VK_R);
        removeButton.addActionListener(actionEvent -> {
            final boolean shouldDelete = JOptionPane.showConfirmDialog(getTopLevelAncestor(), String.format("Are you sure you want to delete %d items?", fileEntries.size()), Util.withTitlePrefix("Delete Confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if(shouldDelete) {
                List<Path> failedPaths = new ArrayList<>();
                for(FileSystemEntityModel model : fileEntries) {
                    try {
                        File file = model.path.toFile();
                        if(file.isDirectory()) {
                            FileUtils.deleteDirectory(file);
                            PWGUI.LOGGER.info("Deleted folder {}", model.path);
                        } else {
                            FileUtils.delete(file);
                        }
                    } catch (IOException e) {
                        failedPaths.add(model.path);
                    }
                }

                if(!failedPaths.isEmpty()) {
                    PWGUI.LOGGER.error("Failed to deleted {} items", failedPaths.size());
                    JOptionPane.showMessageDialog(getTopLevelAncestor(), String.format("Failed to delete the following items:\n%s\nPlease try doing it from an external file manager.", failedPaths.stream().map(e -> e.getFileName().toString() + "\n")));
                }

                PackwizExecutable.INSTANCE.refresh().build().run("File items deleted by user");
            }
        });
        actionButtons.add(removeButton);

        add(actionButtons);
    }
}
