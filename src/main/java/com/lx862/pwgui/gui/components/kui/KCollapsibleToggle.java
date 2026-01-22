package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import javax.swing.*;

/** A button that can be expanded and collapsed */
public class KCollapsibleToggle extends JCheckBox {
    private final String collapsedText;
    private final String expandedText;

    public KCollapsibleToggle(String collapsedText, String expandedText) {
        this.collapsedText = collapsedText;
        this.expandedText = expandedText;

        updateToggle();
        addItemListener(itemEvent -> updateToggle());
    }

    private void updateToggle() {
        setIcon(new ImageIcon(ImageUtil.convertImage(Util.getAssets(isSelected() ? "/assets/ui/up_arrow.png" : "/assets/ui/down_arrow.png"), getFont().getSize())));
        setText(isSelected() ? expandedText : collapsedText);
    }
}
