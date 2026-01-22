package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.dialog.ConsoleDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class OpenPackwizConsoleAction extends AbstractAction {
    private final Window parent;

    public OpenPackwizConsoleAction(Window parent) {
        super("Open Packwiz Console");
        this.parent = parent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConsoleDialog frame = new ConsoleDialog(PackwizExecutable.INSTANCE, parent);
        frame.setVisible(true);
    }
}
