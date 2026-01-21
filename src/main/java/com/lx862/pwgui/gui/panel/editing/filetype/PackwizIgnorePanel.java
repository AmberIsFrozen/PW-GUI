package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.pwcore.Modpack;
import com.lx862.pwgui.core.data.model.GitIgnoreRules;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.core.data.model.file.PackwizIgnoreFileModel;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

public class PackwizIgnorePanel extends FileTypePanel {
    private final GenericFileModel fileEntry;
    private final JTextArea textArea;
    private final FileEntryPaneContext context;
    private String initialContent;

    public PackwizIgnorePanel(FileEntryPaneContext context, PackwizIgnoreFileModel fileEntry) {
        super(context);
        this.context = context;
        this.fileEntry = fileEntry;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel("<html>Packwiz already ignores a handful of files by default. Here, you may specify additional paths to be ignored. Packwiz will not track these files, nor will it be included in the modpack.<br><br>Files that are grayed out on the left pane represents files that are ignored.</html>"); // Use html tag to wrap text
        descriptionLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(descriptionLabel);

        textArea = new JTextArea();
        textArea.setLineWrap(true);

        try {
            String content = fileEntry.getContent();
            updateIgnore(content);
            this.initialContent = content;
            textArea.setText(content);
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
        }
        textArea.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            updateIgnore(textArea.getText());
            updateSaveState();
        }));
        textArea.select(0, 0);
        add(new JScrollPane(textArea));
    }

    private void updateIgnore(String content) {
        GitIgnoreRules gitIgnoreRules = Modpack.defaultFileIgnoreRules.overlay(new GitIgnoreRules(content));
        context.invokeSetTreeIgnorePattern(gitIgnoreRules);
    }

    @Override
    public boolean savable() {
        return true;
    }

    @Override
    public boolean shouldSave() {
        return !initialContent.equals(textArea.getText());
    }

    @Override
    public void save() throws IOException {
        super.save();
        try(FileWriter fw = new FileWriter(fileEntry.path.toFile())) {
            fw.write(textArea.getText());
        }
    }
}
