package com.lx862.pwgui.gui.panel.editing.filetype;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.core.data.model.file.MarkdownFileModel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class MarkdownPanel extends FileTypePanel {

    public MarkdownPanel(FileEntryPaneContext context, MarkdownFileModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        MarkdownPane editorPane = new MarkdownPane();

        try {
            String content = fileEntry.getContent();
            String html = Processor.process(content, Configuration.builder().forceExtentedProfile().build());
            editorPane.setInitialContent(html);
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            editorPane.setText(Util.withBracketPrefix(String.format("Error trying to read file: %s", e.getMessage())));
        }

        add(new JScrollPane(editorPane));
    }

    public static class MarkdownPane extends JEditorPane {
        public MarkdownPane() {
            setEditable(false);
            setContentType("text/html");

            addHyperlinkListener(e -> {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    Util.tryBrowse(e.getURL().toString());
                }
            });
        }

        /** Set content of the panel and stay the caret at the top */
        public void setInitialContent(String str) {
            setText(str);
            setCaretPosition(0);
        }
    }
}