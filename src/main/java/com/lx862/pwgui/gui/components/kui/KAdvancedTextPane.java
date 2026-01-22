package com.lx862.pwgui.gui.components.kui;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.util.Util;
import org.fife.ui.rsyntaxtextarea.FileTypeUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class KAdvancedTextPane extends RSyntaxTextArea {
    public KAdvancedTextPane(String content) {
        super(content);
        setWrapStyleWord(true);
        setLineWrap(true);
        setBracketMatchingEnabled(true);
        setTabSize(4);
        setCaretPosition(0);

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml"));
            theme.apply(this);
        } catch (IOException ignored) {
        }

        // RSyntaxTextArea doesn't zoom the text by default, have to handle it ourselves
        setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() * UIScale.getZoomFactor()));

        addHyperlinkListener(e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Util.tryBrowse(e.getURL().toString());
            }
        });
    }

    public void autoProbeSyntaxHighlighting(File file) {
        setSyntaxEditingStyle(FileTypeUtil.get().guessContentType(file));
    }
}
