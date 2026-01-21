package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.GoUtil;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClearPackwizCacheAction extends AbstractAction {
    private final Component parent;

    public ClearPackwizCacheAction(Component parent) {
        super("Clear Packwiz Cache");
        this.parent = parent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Path packwizCacheDir = GoUtil.getUserCacheDir().resolve("packwiz");
        if(!Files.exists(packwizCacheDir)) {
            JOptionPane.showMessageDialog(parent, "There are currently no packwiz cache yet, nothing to clear~", Util.withTitlePrefix("No Cache Found"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            if(JOptionPane.showConfirmDialog(parent, "Are you sure you want to clear packwiz cache?\nThis is generally not necessary unless you are running out of disk space or encountered some corruption.", Util.withTitlePrefix("Clear Packwiz Cache?"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    FileUtils.deleteDirectory(packwizCacheDir.toFile());
                    JOptionPane.showMessageDialog(parent, "Packwiz cache has been cleared!", Util.withTitlePrefix("Packwiz Cache Cleared!"), JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    PWGUI.LOGGER.error("Failed to clear packwiz cache!", e);
                    JOptionPane.showMessageDialog(parent, String.format("Failed to clear packwiz cache:\n%s", e.getMessage()), Util.withTitlePrefix("Error Clearing Cache"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
