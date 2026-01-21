package com.lx862.pwgui.gui.panel.editing.filetype;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.core.data.model.file.PlainTextFileModel;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;

public class PlainTextPanel extends FileTypePanel {
    private final GenericFileModel fileEntry;
    private final JTextComponent textArea;
    private String initialContent;

    public PlainTextPanel(FileEntryPaneContext context, PlainTextFileModel fileEntry) {
        super(context);
        this.fileEntry = fileEntry;
        setLayout(new BorderLayout());

        String content;
        try {
            content = fileEntry.getContent();
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            content = Util.withBracketPrefix(String.format("Error trying to read file: %s", e.getMessage()));
        }
        this.initialContent = content;

        // TODO: Move to it's own component
        RSyntaxTextArea textArea = new RSyntaxTextArea(content);
        this.textArea = textArea;
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setTabSize(4);
        textArea.setCaretPosition(0);

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml"));
            theme.apply(textArea);
        } catch (IOException ignored) {
        }

        // RSyntaxTextArea doesn't zoom the text by default, have to handle it ourselves
        textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, textArea.getFont().getSize() * UIScale.getZoomFactor()));

        textArea.setSyntaxEditingStyle(FileTypeUtil.get().guessContentType(fileEntry.path.toFile()));
        textArea.getDocument().addDocumentListener(new DocumentChangedListener(this::updateSaveState));

        RTextScrollPane jScrollPane = new RTextScrollPane(textArea);
        jScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        jScrollPane.setAlignmentY(BOTTOM_ALIGNMENT);
        add(jScrollPane, BorderLayout.CENTER);

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
