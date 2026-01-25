package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KActionPanel extends KInlinePanel {

    protected KActionPanel(int flowLayout) {
        super(flowLayout, 4, 0, false);
        setBorder(GUIConfiguration.getPaddedBorder(6, 0, 0, 0));
    }

    public static class Builder {
        private JButton negativeButton = null;
        private JButton positiveButton = null;
        private final int flowLayout;

        private final java.util.List<JComponent> otherComponents;

        public Builder() {
            this(FlowLayout.RIGHT);
        }

        public Builder(int flowLayout) {
            this.flowLayout = flowLayout;
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
            List<JComponent> components = new ArrayList<>();
            KActionPanel actionPanel = new KActionPanel(this.flowLayout);

            for(JComponent component : otherComponents) {
                components.add(component);
            }
            if(positiveButton != null) {
                components.add(positiveButton);
            }
            if(negativeButton != null) {
                components.add(negativeButton);
            }
            for(JComponent component : components) {
                actionPanel.add(component);
            }
            return actionPanel;
        }
    }
}
