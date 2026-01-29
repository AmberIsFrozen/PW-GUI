package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.gui.components.kui.KAdvancedTextPane;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.core.data.model.file.PlainTextFileModel;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;

public class PlainTextPanel extends FileTypePanel {
    private final GenericFileModel fileEntry;
    private final JTextComponent textPane;
    private final String initialContent;

    public PlainTextPanel(FileEntryPaneContext context, PlainTextFileModel fileEntry) {
        super(context);
        this.fileEntry = fileEntry;
        setLayout(new BorderLayout());

        String content;
        try {
            content = fileEntry.getContent();
        } catch (Exception e) {
            PWGUI.LOGGER.error("Error trying to read file!", e);
            content = Util.withBracketPrefix(String.format("Error trying to read file: %s", e.getMessage()));
        }
        this.initialContent = content;

        KAdvancedTextPane textPane = new KAdvancedTextPane(content);
        textPane.autoProbeSyntaxHighlighting(fileEntry.path.toFile());
        this.textPane = textPane;

        textPane.getDocument().addDocumentListener(new DocumentChangedListener(this::updateSaveState));

        RTextScrollPane scrollPane = new RTextScrollPane(textPane);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.setAlignmentY(BOTTOM_ALIGNMENT);
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(actionPanel, BorderLayout.SOUTH);
    }

    @Override
    public boolean savable() {
        return true;
    }

    @Override
    public boolean shouldSave() {
        return !initialContent.equals(textPane.getText());
    }


    @Override
    public void save() throws IOException {
        super.save();
        try(FileWriter fw = new FileWriter(fileEntry.path.toFile())) {
            fw.write(textPane.getText());
        }
    }
}
