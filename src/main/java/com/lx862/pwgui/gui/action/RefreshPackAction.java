package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class RefreshPackAction extends AbstractAction {
    private final Supplier<Component> getParent;

    public RefreshPackAction(Supplier<Component> getParent) {
        super("Refresh Pack Index");
        this.getParent = getParent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_R);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component parent = getParent.get();
        AtomicReference<String> lastLine = new AtomicReference<>();

        Executables.packwiz.refresh().build()
            .onOutput((stdout) -> lastLine.set(stdout.content()))
            .onExit(exitCode -> {
                if(exitCode == 0) {
                    JOptionPane.showMessageDialog(parent, "Modpack index refreshed!", Util.withTitlePrefix("Refresh Modpack Index"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, String.format("Packwiz exited with exit code %d:\n%s", exitCode, lastLine.get()), Util.withTitlePrefix("Refresh Modpack Index"), JOptionPane.ERROR_MESSAGE);
                }
            })
        .run(Strings.REASON_TRIGGERED_BY_USER);
    }
}
