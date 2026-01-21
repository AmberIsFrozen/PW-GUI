package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.core.data.model.GitIgnoreRules;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.core.data.model.file.GitIgnoreFileModel;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

public class GitIgnorePanel extends FileTypePanel {
    private final FileEntryPaneContext context;
    private final GenericFileModel fileEntry;
    private final JTextArea textArea;
    private String initialContent;

    public GitIgnorePanel(FileEntryPaneContext context, GitIgnoreFileModel fileEntry) {
        super(context);
        this.context = context;
        this.fileEntry = fileEntry;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel("<html>All paths in this file will be ignored by Git and will not be included when commiting.<br>Files that are grayed out on the left pane represents files that are ignored.</html>"); // Use html tag to wrap text
        descriptionLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(descriptionLabel);

        this.textArea = new JTextArea();
        this.textArea.setLineWrap(true);
        this.textArea.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            updateIgnore(this.textArea.getText());
            updateSaveState();
        }));

        try {
            String content = fileEntry.getContent();
            GitIgnoreRules gitIgnoreRules = new GitIgnoreRules(content);
            context.invokeSetTreeIgnorePattern(gitIgnoreRules);

            this.initialContent = content;
            this.textArea.setText(content);
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            this.textArea.setText(Util.withBracketPrefix(String.format("Error: %s", e.getMessage())));
        }
        this.textArea.select(0, 0);
        add(new JScrollPane(this.textArea));
    }

    private void updateIgnore(String content) {
        GitIgnoreRules gitIgnoreRules = new GitIgnoreRules(content);
        context.invokeSetTreeIgnorePattern(gitIgnoreRules);
    }

    @Override
    public boolean savable() {
        return true;
    }

    @Override
    public boolean shouldSave() {
        return !this.initialContent.equals(textArea.getText());
    }

    @Override
    public void save() throws IOException {
        super.save();
        try(FileWriter fw = new FileWriter(fileEntry.path.toFile())) {
            fw.write(textArea.getText());
        }
    }
}
