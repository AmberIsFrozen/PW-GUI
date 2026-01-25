package com.lx862.pwgui.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ComboBoxCardLinker<T> {
    private final JComboBox<T> comboBox;
    private final JPanel panel;
    private final CardLayout cardLayout;

    public ComboBoxCardLinker(JComboBox<T> comboBox, JPanel destinatedPanel) {
        this.comboBox = comboBox;
        this.panel = destinatedPanel;
        this.cardLayout = (CardLayout) destinatedPanel.getLayout();
    }

    public void addTab(T data, Container component) {
        this.panel.add(component);
        this.cardLayout.addLayoutComponent(component, data.toString());
        this.comboBox.addItem(data);
    }

    public void apply() {
        this.comboBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                T k = (T)this.comboBox.getSelectedItem();
                if(k == null) return;

                this.cardLayout.show(panel, k.toString());
            }
        });
    }
}
