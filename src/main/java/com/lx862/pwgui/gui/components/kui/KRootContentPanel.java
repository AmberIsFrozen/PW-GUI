package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;

/** A top-level JPanel for layout purposes */
public class KRootContentPanel extends JPanel {
    public KRootContentPanel() {
        super();
    }

    public KRootContentPanel(int padding) {
        super(new BorderLayout());
        setBorder(GUIConfiguration.getPaddedBorder(padding));
    }

    public KRootContentPanel(LayoutManager layout) {
        super(layout);
    }
}
