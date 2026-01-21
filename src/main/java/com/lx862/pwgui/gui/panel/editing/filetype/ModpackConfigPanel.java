package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.core.data.model.file.ModpackConfigFileModel;
import com.lx862.pwgui.pwcore.PackFile;
import com.lx862.pwgui.gui.action.FullUpdateAction;
import com.lx862.pwgui.gui.panel.ModpackExtraSettingPanel;
import com.lx862.pwgui.gui.panel.ModpackInfoPanel;
import com.lx862.pwgui.gui.panel.ModpackVersionPanel;
import com.lx862.pwgui.gui.components.kui.KSeparator;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ModpackConfigPanel extends FileTypePanel {
    private final ModpackInfoPanel infoPanel;
    private final ModpackVersionPanel versionPanel;
    private final ModpackExtraSettingPanel modpackExtraSettingPanel;
    private final PackFile modifiedPackFile;

    public ModpackConfigPanel(FileEntryPaneContext context, ModpackConfigFileModel fileEntry) throws IOException {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        this.modifiedPackFile = new PackFile(fileEntry.packFile.getPath(), fileEntry.packFile.getToml());

        this.infoPanel = new ModpackInfoPanel(modifiedPackFile, this::updateSaveState);
        this.infoPanel.setAlignmentX(LEFT_ALIGNMENT);

        this.versionPanel = new ModpackVersionPanel(modifiedPackFile, this::updateSaveState);
        this.versionPanel.setAlignmentX(LEFT_ALIGNMENT);

        this.modpackExtraSettingPanel = new ModpackExtraSettingPanel(context, modifiedPackFile, this::updateSaveState);
        this.modpackExtraSettingPanel.setAlignmentX(LEFT_ALIGNMENT);

        add(this.infoPanel);
        add(new KSeparator());
        add(this.versionPanel);
        add(new KSeparator());
        add(this.modpackExtraSettingPanel);
        add(Box.createVerticalGlue());
    }

    @Override
    public boolean shouldSave() {
        return this.infoPanel != null && this.versionPanel != null && this.modpackExtraSettingPanel != null && (this.infoPanel.shouldSave() || this.versionPanel.shouldSave() || this.modpackExtraSettingPanel.shouldSave());
    }

    @Override
    public boolean savable() {
        return true;
    }

    @Override
    public void save() throws IOException {
        boolean mcVersionChanged = this.versionPanel.minecraftVersionChanged();
        boolean modloaderChanged = this.versionPanel.modloaderChanged();
        modifiedPackFile.write(Strings.REASON_TRIGGERED_BY_USER);

        if(mcVersionChanged) {
            if(JOptionPane.showConfirmDialog(getTopLevelAncestor(), "Minecraft version has changed.\nDo you want to update the mods as well?", Util.withTitlePrefix("Update Mods"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                new FullUpdateAction(() -> (JFrame)getTopLevelAncestor(), modifiedPackFile).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        } else if(modloaderChanged) {
            if(JOptionPane.showConfirmDialog(getTopLevelAncestor(), "Modloader has been switched.\nDo you want to update the mods as well?", Util.withTitlePrefix("Update Mods"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                new FullUpdateAction(() -> (JFrame)getTopLevelAncestor(), modifiedPackFile).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }
}
