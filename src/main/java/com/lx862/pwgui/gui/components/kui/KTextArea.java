package com.lx862.pwgui.gui.components.kui;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;

import javax.swing.*;
import java.awt.*;

/**
 * A JTextArea with a more sensible tab size & some utility functions
 */
public class KTextArea extends JTextArea {

    public KTextArea() {
        setTabSize(2);
    }

    public void useMonospacedFont() {
        setFont(new Font(Font.MONOSPACED, getFont().getStyle(), (int)(12 * UIScale.getZoomFactor())));
    }

    public void wrapCharacter() {
        setLineWrap(true);
    }

    public void wrapWord() {
        wrapCharacter();
        setWrapStyleWord(true);
    }

    public void onChange(Runnable callback) {
        getDocument().addDocumentListener(new DocumentChangedListener(callback));
    }

    public void setText(String str, boolean jumpToTop) {
        setText(str);
        if(jumpToTop) {
            select(0, 0);
        }
    }
}
