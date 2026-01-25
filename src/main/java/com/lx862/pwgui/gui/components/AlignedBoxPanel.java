package com.lx862.pwgui.gui.components;

import javax.swing.*;
import java.awt.*;

public class AlignedBoxPanel extends JPanel {
    private final float componentAlignment;
    public AlignedBoxPanel(float componentAlignment) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.componentAlignment = componentAlignment;
    }

    @Override
    public Component add(Component comp) {
        if(comp instanceof JComponent jComponent) {
            jComponent.setAlignmentX(componentAlignment);
        }
        return super.add(comp);
    }
}
