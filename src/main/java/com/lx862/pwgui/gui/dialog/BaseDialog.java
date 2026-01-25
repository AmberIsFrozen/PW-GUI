package com.lx862.pwgui.gui.dialog;

import com.lx862.pwgui.core.Config;

import javax.swing.*;
import java.awt.*;

public class BaseDialog extends JDialog {
    public BaseDialog(Window owner, String title) {
        super(owner, title);
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
    }

    public BaseDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public BaseDialog(JDialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    @Override
    public void setSize(int width, int height) {
        float zoom = Config.getInstance().zoomFactor.value();
        super.setSize((int)(width * zoom), (int)(height * zoom));
    }
}
