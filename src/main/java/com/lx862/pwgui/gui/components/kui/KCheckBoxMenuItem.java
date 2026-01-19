package com.lx862.pwgui.gui.components.kui;

import javax.swing.*;
import java.awt.*;

public class KCheckBoxMenuItem extends JCheckBoxMenuItem {
    public KCheckBoxMenuItem(String description) {
        super(description);
        setMargin(new Insets(5, 10, 5, 10));
    }

    public KCheckBoxMenuItem(Action action) {
        super(action);
        setMargin(new Insets(5, 10, 5, 10));
    }
}
