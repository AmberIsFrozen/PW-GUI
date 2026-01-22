package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.support.packwiz.PackFile;
import com.lx862.pwgui.gui.dialog.GenerateModListDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class GenerateModlistAction extends AbstractAction {
    private final Window parent;
    private final PackFile currentPackFile;

    public GenerateModlistAction(Window parent, PackFile currentPackFile) {
        super("Generate Modlist...");
        this.parent = parent;
        this.currentPackFile = currentPackFile;
        putValue(MNEMONIC_KEY, KeyEvent.VK_G);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        new GenerateModListDialog(parent, currentPackFile).setVisible(true);
    }
}
