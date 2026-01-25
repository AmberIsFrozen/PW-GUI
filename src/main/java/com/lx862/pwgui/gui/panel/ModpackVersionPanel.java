package com.lx862.pwgui.gui.panel;

import com.lx862.pwgui.core.data.*;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.gui.components.kui.KGridBagLayoutPanel;
import com.lx862.pwgui.support.packwiz.data.IconNamePair;
import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.data.PackComponentVersion;
import com.lx862.pwgui.support.packwiz.data.VersionMetadata;
import com.lx862.pwgui.support.packwiz.PackFile;
import com.lx862.pwgui.gui.components.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ModpackVersionPanel extends KGridBagLayoutPanel {
    private final JComboBox<VersionMetadata> minecraftVersionComboBox;
    private final JComboBox<VersionMetadata> modloaderVersionComboBox;
    private final JCheckBox showSnapshotCheckBox;
    private final JLabel modloaderVersionLabel;

    private final PackComponentVersion initialMinecraft;
    private final PackComponentVersion initialModloader;

    private PackComponent selectedModloader;

    public ModpackVersionPanel(PackFile existingFile, Runnable updateSaveState) {
        super(3, 2);

        this.initialMinecraft = existingFile == null ? null : existingFile.getComponent(PackComponent.MINECRAFT);
        this.initialModloader = existingFile == null ? null :existingFile.getModloader();
        if(this.initialModloader != null) this.selectedModloader = this.initialModloader.getComponent();

        minecraftVersionComboBox = new JComboBox<>();
        minecraftVersionComboBox.setEditable(true);
        minecraftVersionComboBox.addItemListener(e -> {
            if(existingFile != null) {
                existingFile.setMinecraft(getMinecraft());
                updateSaveState.run();
            }
        });

        modloaderVersionComboBox = new JComboBox<>();
        modloaderVersionComboBox.setEditable(true);
        modloaderVersionComboBox.addItemListener(e -> {
            if(existingFile != null) {
                existingFile.setModloader(getModloader());
                updateSaveState.run();
            }
        });

        if(initialMinecraft != null) minecraftVersionComboBox.setSelectedItem(initialMinecraft.getVersion());
        if(initialModloader != null) modloaderVersionComboBox.setSelectedItem(initialModloader.getVersion());

        addRow(1, new JLabel("Minecraft Version: ", new ImageIcon(ImageUtil.clampImageSize(IconNamePair.MINECRAFT.image(), 20)), SwingConstants.LEFT), minecraftVersionComboBox);

        showSnapshotCheckBox = new JCheckBox("Show Snapshot");
        showSnapshotCheckBox.addActionListener(actionEvent -> updateMinecraftUI());
        addRow(1, null, showSnapshotCheckBox);

        JPanel modloaderChoicePanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));

        modloaderChoicePanel.add(new JLabel("Modloader: "));
        ButtonGroup modloadersButtonGroup = new ButtonGroup();

        JRadioButton vanillaRadioButton = new JRadioButton("None (Vanilla)");
        vanillaRadioButton.addActionListener((itemListener) -> {
            setModloader(null);
            if(existingFile != null) {
                existingFile.setModloader(null);
                updateSaveState.run();
            }
        });
        modloadersButtonGroup.add(vanillaRadioButton);
        modloadersButtonGroup.setSelected(vanillaRadioButton.getModel(), true);
        modloaderChoicePanel.add(vanillaRadioButton);

        for(PackComponent packComponent : PackComponent.values()) {
            if(!packComponent.choosable) continue;

            JRadioButton componentRadioButton = new JRadioButton(packComponent.iconName.name());
            componentRadioButton.addActionListener((itemListener) -> setModloader(packComponent));
            componentRadioButton.setIcon(new ImageIcon(ImageUtil.withOpacity(ImageUtil.clampImageSize(packComponent.iconName.image(), 18), 0.5f)));
            componentRadioButton.setSelectedIcon(new ImageIcon(ImageUtil.clampImageSize(packComponent.iconName.image(), 18)));
            modloaderChoicePanel.add(componentRadioButton);
            modloadersButtonGroup.add(componentRadioButton);

            if(initialModloader != null && initialModloader.getComponent() == packComponent) {
                componentRadioButton.setSelected(true);
            }
        }

        modloaderVersionLabel = new JLabel("Modloader Version: ");

        addRow(2, modloaderChoicePanel);
        addRow(1, modloaderVersionLabel, modloaderVersionComboBox);

        updateMinecraftUI();
        updateModloaderUI();
    }

    private void updateMinecraftUI() {
        // Dummy entry to retain the combo box height account for the list padding
        minecraftVersionComboBox.addItem(new VersionMetadata(null, "Loading...", VersionMetadata.State.RELEASE));

        fetchComponentDatas(PackComponent.MINECRAFT, (versionList) -> {
            boolean showSnapshot = showSnapshotCheckBox.isSelected();
            boolean isSnapshot = versionList.stream().anyMatch(e -> e.getState() != VersionMetadata.State.RELEASE && e.getVersionName().equals(minecraftVersionComboBox.getEditor().getItem().toString()));
            if(isSnapshot) {
                showSnapshot = true;
                showSnapshotCheckBox.setSelected(true);
            }
            fillComboBoxes(null, minecraftVersionComboBox, versionList, showSnapshot);
            // We add event listener after we fill the combobox, as we don't want the fill update to be triggered
            minecraftVersionComboBox.setAction(null);
            minecraftVersionComboBox.addActionListener(actionEvent -> updateModloaderUI());
        });
    }

    private void setModloader(PackComponent modloader) {
        this.selectedModloader = modloader;
        updateModloaderUI();
    }

    private void updateModloaderUI() {
        PackComponent modloader = this.selectedModloader;

        if(modloader == null) {
            modloaderVersionLabel.setVisible(false);
            modloaderVersionComboBox.setVisible(false);
        } else {
            modloaderVersionLabel.setVisible(true);
            modloaderVersionComboBox.setVisible(true);
            modloaderVersionLabel.setText(modloader.iconName.name() + " version:");

            modloaderVersionComboBox.setEnabled(false);
            modloaderVersionComboBox.addItem(new VersionMetadata(null, "Loading...", VersionMetadata.State.RELEASE));
            fetchComponentDatas(modloader, (versionList) -> {
                fillComboBoxes(minecraftVersionComboBox.getEditor().getItem().toString(), modloaderVersionComboBox, versionList, false);
                modloaderVersionComboBox.setEnabled(true);
            });
        }
    }

    public PackComponentVersion getMinecraft() {
        return new PackComponentVersion(PackComponent.MINECRAFT, minecraftVersionComboBox.getEditor().getItem().toString());
    }

    public PackComponentVersion getModloader() {
        return selectedModloader == null ? null : new PackComponentVersion(selectedModloader, modloaderVersionComboBox.getEditor().getItem().toString());
    }

    private void fillComboBoxes(String mcVersion, JComboBox<VersionMetadata> comboBox, List<VersionMetadata> metadatas, boolean showAllReleaseType) {
        Object oldValue = comboBox.getEditor().getItem().toString();
        comboBox.removeAllItems();

        if(metadatas == null) {
            comboBox.setEditable(true);
            return;
        }

        List<VersionMetadata> filteredMetadatas = metadatas.stream()
                .filter(e -> {
                   boolean releaseTypeMatched = showAllReleaseType || e.getState() == VersionMetadata.State.RELEASE;
                   boolean minecraftVersionMatched = e.getAccompaniedMinecraftVersion() == null || e.getAccompaniedMinecraftVersion().equals(mcVersion);
                   return releaseTypeMatched && minecraftVersionMatched;
                })
                .toList();

        int equivalentToOldValueIndex = 0;
        for(int i = 0; i < filteredMetadatas.size(); i++) {
            VersionMetadata metadata = filteredMetadatas.get(i);
            if(metadata.getVersionName().equals(oldValue == null ? "" : oldValue.toString())) equivalentToOldValueIndex = i;
            comboBox.addItem(metadata);
        }

        if(filteredMetadatas.isEmpty()) {
            comboBox.addItem(new VersionMetadata(null, "No version found :(", VersionMetadata.State.RELEASE));
        }

        comboBox.setSelectedIndex(equivalentToOldValueIndex);
    }

    private void fetchComponentDatas(PackComponent packComponent, Consumer<List<VersionMetadata>> callback) {
        if(Caches.componentCaches.get(packComponent) != null) {
            callback.accept(Caches.componentCaches.get(packComponent));
            return;
        }

        try {
            packComponent.versionGetter.get((versionList) -> {
                Caches.componentCaches.put(packComponent, versionList);
                SwingUtilities.invokeLater(() -> callback.accept(versionList));
            });
        } catch (Exception e) {
            Caches.componentCaches.put(packComponent, null);
            callback.accept(null);
        }
    }

    public boolean minecraftVersionChanged() {
        return !Objects.equals(initialMinecraft, getMinecraft());
    }

    public boolean modloaderChanged() {
        PackComponent initialModloaderComponent = initialModloader == null ? null : initialModloader.getComponent();
        PackComponent newModloaderComponent = getModloader() == null ? null : getModloader().getComponent();
        return !Objects.equals(initialModloaderComponent, newModloaderComponent);
    }

    public boolean shouldSave() {
        return !Objects.equals(getMinecraft(), this.initialMinecraft) || !Objects.equals(getModloader(), this.initialModloader);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;
        return size;
    }
}
