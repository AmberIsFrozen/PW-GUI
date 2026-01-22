package com.lx862.pwgui.gui.components.kui;

import javax.swing.*;
import java.awt.*;

/** JButton with the ability to set tooltips */
public class KButton extends JButton {
    public KButton(String description, Icon icon) {
        super(description, icon);
    }

    public KButton(String description) {
        this(description, null);
    }

    public KButton(Action action) {
        super(action);
    }

    public void setEnabled(boolean value, String disabledReason) {
        if(!value) {
            setToolTipText(disabledReason);
        } else {
            setToolTipText(null);
        }
        super.setEnabled(value);
    }
}
