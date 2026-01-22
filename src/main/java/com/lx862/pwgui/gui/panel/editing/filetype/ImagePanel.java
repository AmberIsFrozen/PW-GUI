package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.gui.components.kui.KImageDisplayPanel;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends FileTypePanel {
    public ImagePanel(FileEntryPaneContext context, GenericFileModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        KImageDisplayPanel imagePreviewPanel = new KImageDisplayPanel(fileEntry.path.toFile());
        imagePreviewPanel.setAlignmentX(LEFT_ALIGNMENT);

        BufferedImage image = imagePreviewPanel.getImage();

        if(image != null) {
            JLabel desc = new JLabel(String.format("Dimensions: %dx%d", image.getWidth(), image.getHeight()));
            desc.setAlignmentX(LEFT_ALIGNMENT);
            add(desc);
            add(GUIConfiguration.createVerticalPadding(8));
        }

        add(imagePreviewPanel);
    }
}
