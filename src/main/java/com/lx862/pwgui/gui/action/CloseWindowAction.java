package com.lx862.pwgui.gui.action;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CloseWindowAction extends AbstractAction {
    private final Window parent;

    public CloseWindowAction(String title, Window parent) {
        super(title);
        this.parent = parent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        parent.dispose();
    }
}
