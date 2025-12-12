package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.core.data.Caches;
import com.lx862.pwgui.core.Constants;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.pwcore.data.PackComponent;
import com.lx862.pwgui.pwcore.data.VersionMetadata;
import com.lx862.pwgui.executable.BatchedProgramExecution;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.gui.action.CloseWindowAction;
import com.lx862.pwgui.gui.components.ToggleListSelectionModel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KListCellRenderer;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChangeAcceptableGameVersionDialog extends JDialog {
    private final List<VersionMetadata> versions;

    public ChangeAcceptableGameVersionDialog(JFrame parentFrame, String requiredVersion, List<String> preSelectedVersions, Runnable saveCallback) {
        super(parentFrame, Util.withTitlePrefix("Change Acceptable Version"), true);

        setSize(420, 400);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.versions = new ArrayList<>();

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel selectedVersionLabel = new JLabel("Selected version(s): ???");

        JLabel titleLabel = new JLabel("Change Acceptable Version");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h3.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel("<html>Packwiz filters contents from Modrinth/CurseForge based on your Minecraft version.<br>Here, you can add additional versions that should be recognized.<br>This could be useful when including cross-compatible versions.<br>(e.g. 1.18, 1.18.1, 1.18.2)</html>");
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        KButton okButton = new KButton("OK");
        okButton.setMnemonic(KeyEvent.VK_O);

        JCheckBox snapshotCheckBox = new JCheckBox("Show Snapshot");

        JList<String> versionList = new JList<>(new DefaultListModel<>());
        versionList.setCellRenderer(new KListCellRenderer());
        versionList.setSelectionModel(new ToggleListSelectionModel());
        versionList.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionList.addListSelectionListener(listSelectionEvent -> {
            okButton.setEnabled(versionList.getSelectedValuesList().contains(requiredVersion));
            selectedVersionLabel.setText(String.format("Selected version(s): %s", String.join(", ", versionList.getSelectedValuesList())));
        });

        JScrollPane versionListScrollPane = new JScrollPane(versionList);
        versionListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(versionListScrollPane);

        Caches.getVersionMetadata(PackComponent.MINECRAFT, (versions) -> {
            this.versions.clear();
            this.versions.addAll(versions);
            refreshVersionList(versionList, preSelectedVersions, snapshotCheckBox);
        });


        snapshotCheckBox.addActionListener(actionEvent -> refreshVersionList(versionList, versionList.getSelectedValuesList(), snapshotCheckBox));
        contentPanel.add(snapshotCheckBox);
        contentPanel.add(selectedVersionLabel);

        okButton.addActionListener(actionEvent -> {
            okButton.setEnabled(false);
            // Because we directly run packwiz to change the version, it would bypass the regular save procedure and would overwrite the file directly, discarding any unsaved changes
            // We should prompt for saving to avoid any data loss (Ideally we should write the changes ourselves, but meh :P)
            saveCallback.run();
            changeAcceptableVersion(preSelectedVersions, versionList.getSelectedValuesList(), (success) -> {
                if(success) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Some versions did not get added due to errors.\nPlease check log for details.", Util.withTitlePrefix("Error"), JOptionPane.ERROR_MESSAGE);
                    okButton.setEnabled(true);
                }
            });
        });

        KButton cancelButton = new KButton(new CloseWindowAction("Cancel", this));

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(okButton).setNegativeButton(cancelButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(actionPanel);

        add(contentPanel);
    }

    private boolean containsSnapshot(List<String> selected, List<VersionMetadata> versions) {
        List<VersionMetadata> snapshotVersions = versions.stream().filter(e -> e.getState() != VersionMetadata.State.RELEASE).toList();
        for(VersionMetadata versionMetadata : snapshotVersions) {
            if(selected.contains(versionMetadata.getVersionName())) return true;
        }
        return false;
    }

    private void changeAcceptableVersion(List<String> oldVersionList, List<String> newVersionList, Consumer<Boolean> callback) {
        List<String> toBeRemoved = oldVersionList.stream().filter(e -> !newVersionList.contains(e)).toList();
        List<String> toBeAdded = newVersionList.stream().filter(e -> !oldVersionList.contains(e)).toList();

        BatchedProgramExecution batchedProgramExecution = new BatchedProgramExecution();

        for(String version : toBeRemoved) {
            batchedProgramExecution.add(Executables.packwiz.settings().removeAcceptableVersions(version).build());
        }
        for(String version : toBeAdded) {
            batchedProgramExecution.add(Executables.packwiz.settings().addAcceptableVersions(version).build());
        }

        batchedProgramExecution.onExit(callback);
        batchedProgramExecution.execute(Constants.REASON_TRIGGERED_BY_USER);
    }

    private void refreshVersionList(JList<String> jList, List<String> selected, JCheckBox snapshotCheckBox) {
        if(!snapshotCheckBox.isSelected() && containsSnapshot(selected, versions)){
            snapshotCheckBox.setSelected(true);
        }
        boolean allowSnapshot = snapshotCheckBox.isSelected();

        DefaultListModel<String> listModel = (DefaultListModel<String>)jList.getModel();
        listModel.removeAllElements();

        List<Integer> selectedIndexes = new ArrayList<>();

        int i = 0;
        for(VersionMetadata version : versions) {
            if(version.getState() == VersionMetadata.State.ALPHA && !allowSnapshot) continue;

            listModel.addElement(version.getVersionName());
            if(selected.contains(version.getVersionName())) selectedIndexes.add(i);
            i++;
        }

        jList.setSelectedIndices(selectedIndexes.stream().mapToInt(Integer::intValue).toArray());
        jList.ensureIndexIsVisible(jList.getMinSelectionIndex()); // Scroll selected to view
    }
}
