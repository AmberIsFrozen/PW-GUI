package com.lx862.pwgui.gui.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** A document listener that fires a callback when anything has changed */
public class DocumentChangedListener implements DocumentListener {
    private final Runnable callback;

    public DocumentChangedListener(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        callback.run();
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        callback.run();
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        callback.run();
    }
}
