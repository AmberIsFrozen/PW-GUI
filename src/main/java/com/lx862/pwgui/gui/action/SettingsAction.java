package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.gui.dialog.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SettingsAction extends AbstractAction {
    private final Window parent;

    public SettingsAction(Window parent) {
        super("Settings");
        this.parent = parent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_S);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SettingsDialog(Config.getInstance(), parent).setVisible(true);
    }
}
