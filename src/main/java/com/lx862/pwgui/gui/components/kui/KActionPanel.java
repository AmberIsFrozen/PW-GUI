package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class KActionPanel extends JPanel {

    protected KActionPanel() {
        super(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        setBorder(GUIConfiguration.getPaddedBorder(6, 0, 0, 0));
    }

    public static class Builder {
        private JButton negativeButton = null;
        private JButton positiveButton = null;

        private final java.util.List<JComponent> otherComponents;

        public Builder() {
            this.otherComponents = new ArrayList<>();
        }

        public Builder setNegativeButton(JButton button) {
            this.negativeButton = button;
            return this;
        }

        public Builder setPositiveButton(JButton button) {
            this.positiveButton = button;
            return this;
        }

        public Builder add(JComponent... components) {
            otherComponents.addAll(Arrays.asList(components));
            return this;
        }

        public KActionPanel build() {
            KActionPanel actionPanel = new KActionPanel();
            for(JComponent component : otherComponents) {
                actionPanel.add(component);
            }
            if(positiveButton != null) {
                actionPanel.add(positiveButton);
            }
            if(negativeButton != null) {
                actionPanel.add(negativeButton);
            }
            return actionPanel;
        }
    }
}
