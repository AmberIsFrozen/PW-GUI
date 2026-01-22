package com.lx862.pwgui.gui.panel.editing;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.NameTabPair;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KTabbedPane;
import com.lx862.pwgui.gui.panel.editing.filetype.*;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FileDetailPanel extends JPanel {
    private final KTabbedPane fileTabPane;
    public final KButton saveButton;

    public FileDetailPanel() {
        setLayout(new BorderLayout());

        this.fileTabPane = new KTabbedPane();
        this.saveButton = new KButton("Save");
        saveButton.setVisible(false);

        saveButton.addActionListener(actionEvent -> saveTab((JPanel)this.fileTabPane.getSelectedComponent(), true));
        fileTabPane.addChangeListener(changeEvent -> {
            saveButton.setEnabled(fileTabPane.getSelectedComponent() instanceof FileTypePanel panel && panel.shouldSave());
            saveButton.setVisible(fileTabPane.getSelectedComponent() instanceof FileTypePanel panel && panel.savable());
        });

        JPanel fileEntryActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0 ,0));
        fileEntryActionRow.add(saveButton);
        add(fileTabPane, BorderLayout.CENTER);
        add(fileEntryActionRow, BorderLayout.SOUTH);
        setTabs(Collections.emptyList());
    }

    public void setTabs(List<NameTabPair> tabs) {
        if(tabs.isEmpty()) {
            JPanel noSelectionPanel1 = new NoSelectionPanel();
            fileTabPane.setTabs(List.of(new NameTabPair("No item selected", noSelectionPanel1)));
        } else {
            fileTabPane.setTabs(tabs);
        }
    }

    public void saveAllTabs(boolean showUnsavedPrompt) {
        boolean haveUnsaved = false;

        for(int i = 0; i < fileTabPane.getTabCount(); i++) {
            Component component = fileTabPane.getComponent(i);
            if(component instanceof FileTypePanel) {
                if(((FileTypePanel) component).shouldSave()) haveUnsaved = true;
            }
        }

        if(!haveUnsaved) return;

        if(showUnsavedPrompt) {
            if(JOptionPane.showConfirmDialog(getTopLevelAncestor(), "You have edited files that are not saved.\nDo you want to save changes?", Util.withTitlePrefix("Unsaved Changes"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        for(int i = 0; i < fileTabPane.getTabCount(); i++) {
            Component component = fileTabPane.getComponent(i);
            if(component instanceof FileTypePanel filePanel) {
                saveTab(filePanel, false);
            }
        }

        // Refresh packwiz after saving file
        PackwizExecutable.INSTANCE.refresh().build().run(String.format("File modified by %s", ApplicationInfo.INSTANCE.name));
    }

    private void saveTab(JPanel panel, boolean shouldRefresh) {
        if(panel instanceof FileTypePanel fileTypePanel && fileTypePanel.shouldSave()) {
            try {
                fileTypePanel.save();
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(this, String.format("Failed to save file!\n%s", e.getMessage()), Util.withTitlePrefix("Failed to Save!"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(shouldRefresh) PackwizExecutable.INSTANCE.refresh().build().run("Save file");
        }
    }

    static class NoSelectionPanel extends JPanel {
        public NoSelectionPanel() {
            super(new BorderLayout());
            JLabel emptyLabel = new JLabel("<html><p style=\"text-align: center\">No items selected!<br>Select an item on the left to view & perform operations.</p></html>");
            emptyLabel.setHorizontalAlignment(SwingConstants.HORIZONTAL);
            add(emptyLabel, BorderLayout.CENTER);
        }
    }
}
