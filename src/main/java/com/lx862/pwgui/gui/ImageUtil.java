package com.lx862.pwgui.gui;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.PWGUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageUtil {
    public static final Image MISSING_TEXTURE = getMissingTextureImage();

    public static Image clampImageSize(Image img, int size) {
        double imgWidth = img.getWidth(null);
        double imgHeight = img.getHeight(null);
        double ratioW = 1;
        double ratioH = 1;
        if(imgWidth > size) {
            ratioW = size / imgWidth;
        }
        if(imgHeight > size) {
            ratioH = size / imgHeight;
        }
        double minRatio = Math.min(ratioW, ratioH);

        return resizeImage(img, (int)(imgWidth * minRatio), (int)(imgHeight * minRatio), Image.SCALE_SMOOTH);
    }

    public static Image resizeImage(Image img, int width, int height, int scaleMode) {
        int zoomW = (int)(width * UIScale.getZoomFactor());
        int zoomH = (int)(height * UIScale.getZoomFactor());
        return img.getScaledInstance(zoomW, zoomH, scaleMode);
    }

    /** Returns a new image with the specified opacity */
    public static Image withOpacity(Image image, float opacity) {
        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)newImage.getGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(opacity));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static Image convertImage(InputStream is, int size) {
        Image img = convertImage(is);
        return clampImageSize(img, size);
    }

    public static Image convertImage(InputStream is) {
        try {
            return ImageIO.read(is);
        } catch (Exception e) {
            PWGUI.LOGGER.error("Failed to load image!", e);
            return MISSING_TEXTURE;
        }
    }

    private static Image getMissingTextureImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xFF00DC));
        g.fillRect(0, 0, 8, 8);
        g.fillRect(8, 8, 8, 8);
        g.setColor(Color.BLACK);
        g.fillRect(8, 0, 8, 8);
        g.fillRect(0, 8, 8, 8);
        g.dispose();
        return image;
    }
}
