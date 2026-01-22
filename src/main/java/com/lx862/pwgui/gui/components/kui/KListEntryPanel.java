package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;

public class KListEntryPanel extends JPanel {

    public KListEntryPanel() {
        this(null);
    }

    public KListEntryPanel(String title) {
        setBorder(GUIConfiguration.borderWithPadding(4, GUIConfiguration.getSeparatorBorder(false, true)));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(new JTextField().getBackground());

        if(title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            add(titleLabel);
        }
    }



    @Override
    public Component add(Component comp) {
        if(comp instanceof JComponent) {
            ((JComponent)comp).setAlignmentX(LEFT_ALIGNMENT);
        }
        return super.add(comp);
    }
}
