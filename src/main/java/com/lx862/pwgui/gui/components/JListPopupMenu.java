package com.lx862.pwgui.gui.components;

import javax.swing.*;
import java.awt.*;

public class JListPopupMenu extends JPopupMenu {
    private final JList<?> list;

    public JListPopupMenu(JList<?> list) {
        this.list = list;
    }

    @Override
    public void show(Component invoker, int x, int y) {
        int idx = list.locationToIndex(new Point(x, y));
        if(idx != -1) {
            list.setSelectedIndex(idx);
        }
        super.show(invoker, x, y);
    }
}
