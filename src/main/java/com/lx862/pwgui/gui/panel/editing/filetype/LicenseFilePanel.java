package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.action.EditLicenseAction;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KTextArea;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.core.data.model.file.PlainTextFileModel;

import javax.swing.*;

public class LicenseFilePanel extends FileTypePanel {
    public LicenseFilePanel(FileEntryPaneContext context, PlainTextFileModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        KButton changeLicenseButton = new KButton(new EditLicenseAction(() -> (JFrame)getTopLevelAncestor(), fileEntry.path.toFile()));
        changeLicenseButton.setAlignmentX(LEFT_ALIGNMENT);
        add(changeLicenseButton);

        KTextArea textArea = new KTextArea();
        textArea.setEditable(false);
        textArea.wrapWord();

        try {
            String content = fileEntry.getContent();
            textArea.setText(content, true);
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            textArea.setText(Util.withBracketPrefix(String.format("Error trying to read file: %s", e.getMessage())), true);
        }

        JScrollPane jScrollPane = new JScrollPane(textArea);
        jScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(jScrollPane);
    }
}
