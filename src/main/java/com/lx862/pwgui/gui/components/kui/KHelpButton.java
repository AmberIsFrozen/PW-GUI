package com.lx862.pwgui.gui.components.kui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

/* A button that displays more detailed information about a control field */
public class KHelpButton extends JButton {
    public KHelpButton(String detailedInfo) {
        super("Help");
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_HELP);
        setToolTipText("What is this?");
        addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(getTopLevelAncestor(), detailedInfo, "What is this?", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
