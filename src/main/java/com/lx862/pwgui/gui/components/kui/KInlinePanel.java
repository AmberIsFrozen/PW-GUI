package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.gui.components.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class KInlinePanel extends JPanel {
    private final int hGap;

    public KInlinePanel(int flowLayout, int hGap, int vGap) {
        setLayout(new WrapLayout(flowLayout, 0, vGap));
        this.hGap = hGap;
    }

    public KInlinePanel(int flowLayout) {
        this(flowLayout, 4, 4);
    }

    public KInlinePanel() {
        this(FlowLayout.LEFT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
    }

    @Override
    public Component add(Component component) {
        int alignment = ((FlowLayout)getLayout()).getAlignment();
        if(alignment == FlowLayout.RIGHT || alignment == FlowLayout.TRAILING) {
            super.add(GUIConfiguration.createHorizontalPadding(hGap));
        }

        super.add(component);

        if(alignment == FlowLayout.LEFT || alignment == FlowLayout.LEADING) {
            super.add(GUIConfiguration.createHorizontalPadding(hGap));
        }
        return component;
    }
}
