package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.io.IOException;

public abstract class FileTypePanel extends JPanel {
    private final FileEntryPaneContext context;

    public FileTypePanel(FileEntryPaneContext context) {
        this.context = context;
        setBorder(GUIConfiguration.getPaddedBorder(6));
    }

    public boolean shouldSave() {
        return false;
    }

    public boolean savable() {
        return false;
    }

    protected void updateSaveState() {
        context.setShouldSave(shouldSave());
    }
    
    public void save() throws IOException {
        context.setShouldSave(false);
    }
}
