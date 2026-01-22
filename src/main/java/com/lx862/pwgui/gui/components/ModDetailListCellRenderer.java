package com.lx862.pwgui.gui.components;

import com.lx862.pwgui.support.packwiz.PackwizMetaFile;
import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class ModDetailListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> jList, Object item, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(jList, item, index, isSelected, cellHasFocus);
        setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(128, 128, 128)),
                GUIConfiguration.getPaddedBorder(3)
        ));

        if (item instanceof PackwizMetaFile packwizMetaFile) {
            setText(String.format("<html><b>%s</b><br><b>File Name:</b> %s</html>", packwizMetaFile.name, packwizMetaFile.fileName));
        }

        return this;
    }
}
