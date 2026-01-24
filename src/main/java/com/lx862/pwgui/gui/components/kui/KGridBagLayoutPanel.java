package com.lx862.pwgui.gui.components.kui;

import javax.swing.*;
import java.awt.*;

/** Wrapper for panel with the GridBagLayout. Designed for form-like input */
public class KGridBagLayoutPanel extends JPanel {
    private final GridBagConstraints gbc;
    private final int maxWidthSpan;
    private int y;

    public KGridBagLayoutPanel(int insets, int maxWidthSpan) {
        this(insets, insets, maxWidthSpan);
    }

    public KGridBagLayoutPanel(int insetW, int insetH, int maxWidthSpan) {
        setLayout(new GridBagLayout());
        this.maxWidthSpan = maxWidthSpan;
        this.gbc = new GridBagConstraints();
        this.gbc.insets = new Insets(insetH, insetW, insetH, insetW);
        this.gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    public void addRow(int widthSpan, Component... components) {
        int x = 0;

        for(int i = 0; i < components.length; i++) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.weightx = i == maxWidthSpan-1 ? 1 : 0; // Fill last row
            gbc.weighty = 0;
            gbc.gridwidth = widthSpan;
            if(components[i] != null) add(components[i], gbc);
            x++;
        }
        y++;
    }

    public void addRow(int widthSpan, int fullWeightIndex, Component... components) {
        int x = 0;

        for(int i = 0; i < components.length; i++) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.weightx = i == fullWeightIndex ? 1 : 0;
            gbc.weighty = 0;
            gbc.gridwidth = widthSpan;
            add(components[i], gbc);
            x++;
        }
        y++;
    }

    public void addVerticalFiller() {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weighty = 1;
        gbc.gridwidth = maxWidthSpan;
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        add(panel, gbc);
    }
}
