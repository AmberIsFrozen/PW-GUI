package com.lx862.pwgui.gui.components.kui;

import javax.swing.*;
import java.awt.*;

/** A variant of JTextField with placeholder text support */
public class KTextField extends JTextField {
    private final String placeholder;
    private final boolean moveCaretToFront;

    /**
     * Create a new KTextField
     * @param placeholderText Placeholder text, can be null
     * @param moveCaretToFront Whether to move the cursor/caret to the front when using {@link KTextField#setText}
     */
    public KTextField(String placeholderText, boolean moveCaretToFront) {
        this.placeholder = placeholderText;
        this.moveCaretToFront = moveCaretToFront;
    }

    /**
     * Create a new KTextField.
     * @param placeholderText Placeholder text, can be null
     */
    public KTextField(String placeholderText) {
        this(placeholderText, false);
    }

    public KTextField() {
        this(null);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if(placeholder != null && !placeholder.isEmpty() && getText().isEmpty()) {
            float y = getInsets().top + g2d.getFontMetrics().getMaxAscent();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getDisabledTextColor());
            g2d.drawString(placeholder, getInsets().left, y);
        }
    }

    @Override
    public void setText(String str) {
        super.setText(str);

        if(moveCaretToFront) {
            setSelectionStart(0);
            setSelectionEnd(0);
        }
    }
}
