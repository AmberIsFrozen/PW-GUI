package com.lx862.pwgui.gui;

import com.formdev.flatlaf.FlatLaf;
import com.lx862.pwgui.core.data.ApplicationTheme;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class GUIConfiguration {
    private static final Parser MARKDOWN_PASER = new Parser.Builder().extensions(List.of(StrikethroughExtension.create(), TablesExtension.create())).build();
    private static final HtmlRenderer HTML_RENDERER = new HtmlRenderer.Builder().extensions(List.of(StrikethroughExtension.create(), TablesExtension.create())).build();

    /**
     * Setup FlatLaf and apply the application theme for the window
     * @param applicationTheme The application theme to change to
     * @param useWindowDecoration Whether window decoration (Title bar) should be used
     * @param window The window to change, null if the theme should apply globally to all swing window
     */
    public static void setupGUI(ApplicationTheme applicationTheme, boolean useWindowDecoration, Window window) {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.setup(applicationTheme.getLaf());

            System.setProperty("flatlaf.useWindowDecorations", useWindowDecoration ? "true" : "false");

            UIManager.put("Component.focusWidth", 1);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("ScrollBar.width", 14);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("Button.arc", 9);
            UIManager.put("Button.margin", new Insets(5, 15, 5, 15));
            UIManager.put("TextField.margin", new Insets(5, 9, 5, 9));
            UIManager.put("ComboBox.padding", new Insets(5, 7, 5, 7));
            UIManager.put("ComboBox.popupInsets", new Insets(3, 3, 3, 3));
            UIManager.put("ComboBox.selectionArc", 7);
            UIManager.put("ScrollPane.arc", 9);
            UIManager.put("TextComponent.arc", 7);
            UIManager.put("Menu.margin", new Insets(5, 10, 5, 10));
            UIManager.put("MenuItem.margin", new Insets(5, 10, 5, 10));
            UIManager.put("CheckBoxMenuItem.margin", new Insets(5, 10, 5, 10));
            UIManager.put("MenuBar.itemMargins", new Insets(5, 8, 5, 8));
            UIManager.put("List.selectionArc", 6);
            UIManager.put("List.border", getPaddedBorder(3, 0, 3, 0));
            UIManager.put("List.cellMargins", new Insets(5, 8, 5, 8));
            UIManager.put("Tree.selectionArc", 6);
            UIManager.put("Tree.border", getPaddedBorder(3, 0, 3, 0));
            UIManager.put("Tree.rendererMargins", new Insets(6, 0, 6, 0));
            UIManager.put("Component.arc", 6);
            UIManager.put("Component.hideMnemonics", false);

            ToolTipManager.sharedInstance().setInitialDelay(300);

            // Don't dismiss the tooltip when user is still hovering
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

            if(window == null) { // Every window
                for(Window subWindow : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(subWindow);
                }
            } else {
                SwingUtilities.updateComponentTreeUI(window);
            }
        });
    }

    public static Component createHorizontalPadding(int width) {
        return Box.createRigidArea(new Dimension(width, 0));
    }

    public static Component createVerticalPadding(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    public static Border borderWithPadding(int padding, Border border) {
        return new CompoundBorder(border, getPaddedBorder(padding));
    }

    public static Border getPaddedBorder(int padding) {
        return new EmptyBorder(padding, padding, padding, padding);
    }

    public static Border getPaddedBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static Border getSeparatorBorder(boolean top, boolean bottom) {
        return new MatteBorder(top ? 1 : 0, 0, bottom ? 1 : 0, 0, getBorderColor());
    }

    public static Color getBorderColor() {
        return UIManager.getColor("Component.borderColor");
    }

    public static String markdownToHtml(String md) {
        return HTML_RENDERER.render(MARKDOWN_PASER.parse(md));
    }
}
