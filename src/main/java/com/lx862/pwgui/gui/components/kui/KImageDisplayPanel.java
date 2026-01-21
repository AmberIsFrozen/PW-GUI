package com.lx862.pwgui.gui.components.kui;

import com.lx862.pwgui.PWGUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/** An image preview panel */
public class KImageDisplayPanel extends JPanel {
    private BufferedImage image;

    public KImageDisplayPanel(File file) {
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
            add(new JLabel(String.format("Error while reading the image: %s", e.getMessage())));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image == null) return;

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scaleFactor = 1;
        double maxW = getWidth() - getInsets().right - getInsets().left;
        double maxH = getHeight() - getInsets().top - getInsets().bottom;
        if(image.getWidth() >= maxW) {
            scaleFactor = maxW / image.getWidth();
        }
        if(image.getHeight() >= maxH) {
            scaleFactor = Math.min(scaleFactor, maxH / image.getHeight());
        }

        g.drawImage(image, getInsets().left, getInsets().top, (int)(image.getWidth() * scaleFactor), (int)(image.getHeight() * scaleFactor), this);
    }

    public BufferedImage getImage() {
        return image;
    }
}
