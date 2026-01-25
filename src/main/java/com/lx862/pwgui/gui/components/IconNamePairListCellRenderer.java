package com.lx862.pwgui.gui.components;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.support.packwiz.data.IconNamePair;

import javax.swing.*;
import java.awt.*;

public class IconNamePairListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> jList, Object item, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(jList, item, index, isSelected, cellHasFocus);

        if(item instanceof IconNamePair iconNamePair) {
            setIcon(new ImageIcon(ImageUtil.clampImageSize(iconNamePair.image(), 20)));
            setText(iconNamePair.name());
        }

        return this;
    }
}
