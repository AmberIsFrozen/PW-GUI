package com.lx862.pwgui.gui.panel;

import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.PackFile;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.dialog.ChangeAcceptableGameVersionDialog;
import com.lx862.pwgui.gui.panel.editing.filetype.FileEntryPaneContext;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class ModpackExtraSettingPanel extends KGridBagLayoutPanel {
    private final String initialDescription;
    private final Path initialDatapackPath;
    private final PackFile existingFile;

    private final KTextField modpackDescriptionTextField;

    private final Runnable updateSaveState;

    public ModpackExtraSettingPanel(FileEntryPaneContext context, PackFile existingFile, Runnable updateSaveState) {
        super(3, 3);

        this.existingFile = existingFile;
        this.updateSaveState = updateSaveState;
        this.initialDescription = existingFile.description;
        this.initialDatapackPath = existingFile.getDatapackPath();

        modpackDescriptionTextField = new KTextField("Modpack description...", true);
        modpackDescriptionTextField.setText(this.initialDescription);
        modpackDescriptionTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            existingFile.description = modpackDescriptionTextField.getText();
            updateSaveState.run();
        }));

        KGridBagLayoutPanel modpackDescriptionPanel = new KGridBagLayoutPanel(0, 3);
        modpackDescriptionPanel.addRow(1, 1, new JLabel("Description: "), modpackDescriptionTextField, new KHelpButton("Here you can enter a short description of the modpack. \nThis is only used when exporting to a Modrinth modpack, and not in use elsewhere."));
        addRow(2, modpackDescriptionPanel);

        KButton changeVersionRangeButton = new KButton("Change...");
        changeVersionRangeButton.addActionListener(actionEvent -> {
            new ChangeAcceptableGameVersionDialog((JFrame) getTopLevelAncestor(), existingFile.getComponent(PackComponent.MINECRAFT).getVersion(), existingFile.getOptionAcceptableGameVersion(true), context::promptForSave).setVisible(true);
        });

        addRow(1, 0, new JLabel("Acceptable Minecraft Version: " + String.join(", ", existingFile.getOptionAcceptableGameVersion(true))), changeVersionRangeButton);

        JLabel datapackDirectoryLabel = new JLabel(getDatapackFolderString());
        KButton changeDatapackDirectoryButton = new KButton("Change...");
        changeDatapackDirectoryButton.addActionListener(actionEvent -> changeDatapackDirectory(context.getModpack(), existingFile, datapackDirectoryLabel));
        addRow(1, 0, datapackDirectoryLabel, changeDatapackDirectoryButton);
    }

    private void changeDatapackDirectory(Modpack modpack, PackFile existingFile, JLabel datapackFolderLabel) {
        KFileChooser fileChooser = new KFileChooser("datapack-location", modpack.getRootPath());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if(fileChooser.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if(!Util.withinDirectory(modpack.getRootPath(), selectedFile.toPath())) {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), "Folder must be inside modpack folder!", Util.withTitlePrefix("Invalid Selected Directory"), JOptionPane.ERROR_MESSAGE);
                changeDatapackDirectory(modpack, existingFile, datapackFolderLabel);
                return;
            }
            existingFile.setDatapackPath(modpack.getRootPath().relativize(selectedFile.toPath()));
            datapackFolderLabel.setText(getDatapackFolderString());
            updateSaveState.run();
        }
    }

    private String getDatapackFolderString() {
        return String.format("Datapack Folder: %s", existingFile.getDatapackPath() == null ? "(Not Configured)" : existingFile.getDatapackPath());
    }

    public boolean shouldSave() {
        return !Objects.equals(initialDatapackPath, existingFile.getDatapackPath()) || !Objects.equals(initialDescription, modpackDescriptionTextField.getText());
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;
        return size;
    }
}
