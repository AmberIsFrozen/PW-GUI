package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.core.data.model.file.ModpackIndexFileModel;
import com.lx862.pwgui.gui.action.RefreshPackAction;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;

public class ModpackIndexFilePanel extends FileTypePanel {
    public ModpackIndexFilePanel(FileEntryPaneContext context, ModpackIndexFileModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel("<html>The index file helps packwiz keep track of each file in the pack, including its hashes to ensure file integrity.</html>");
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(descriptionLabel);

        add(GUIConfiguration.createVerticalPadding(10));

        JLabel description2Label = new JLabel("<html>Most of the functionalities are already available in the GUI. As such there should be no need to manually edit this file.</html>");
        description2Label.setAlignmentX(LEFT_ALIGNMENT);
        add(description2Label);

        add(GUIConfiguration.createVerticalPadding(10));

        JLabel description3Label = new JLabel(String.format("<html>If you have added files outside of %s, you can press the refresh button below to keep the index up to date.</html>", ApplicationInfo.INSTANCE.name));
        description3Label.setAlignmentX(LEFT_ALIGNMENT);
        add(description3Label);

        add(GUIConfiguration.createVerticalPadding(10));

        KButton refreshButton = new KButton(new RefreshPackAction(this::getTopLevelAncestor));
        refreshButton.setAlignmentX(LEFT_ALIGNMENT);
        add(refreshButton);
    }
}
