package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.components.kui.KTabbedPane;
import com.lx862.pwgui.gui.panel.editing.filetype.MarkdownPanel;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AboutDialog extends BaseDialog {
    public AboutDialog(Window parent) {
        super(parent, String.format("About %s", ApplicationInfo.INSTANCE.name), ModalityType.DOCUMENT_MODAL);
        setSize(325, 500);
        setLocationRelativeTo(parent);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        JPanel mainPanel = new MainPanel();
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel);
    }

    static class MainPanel extends JPanel {
        public MainPanel() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel logoLabel = new JLabel(new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/logo.png"), 200), "Application Logo"));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(logoLabel);

            JLabel titleLabel = new JLabel(ApplicationInfo.INSTANCE.name);
            titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(titleLabel);

            JLabel descriptionLabel = new JLabel("<html><div style='text-align:center'>GUI Wrapper for packwiz, a tool to edit & distribute Minecraft modpacks.</div></html>", SwingConstants.CENTER);
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(descriptionLabel);

            JLabel versionLabel = new JLabel(ApplicationInfo.INSTANCE.version);
            versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(versionLabel);

            add(GUIConfiguration.createVerticalPadding(8));

            KTabbedPane tabbedPane = new KTabbedPane();
            tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_FILL );
            tabbedPane.addTab("External Links", new JScrollPane(new FileTabPane("links.md")));
            tabbedPane.addTab("Details", new JScrollPane(new FileTabPane("details.md")));
            tabbedPane.addTab("Credits", new JScrollPane(new FileTabPane("credits.md")));
            tabbedPane.addTab("Duke", new JScrollPane(new FileTabPane("duke.md")));

            add(tabbedPane);

            JLabel footerLabel = new JLabel(String.format("%s 2026 <3", ApplicationInfo.INSTANCE.author));
            footerLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(footerLabel);
        }

        static class FileTabPane extends MarkdownPanel.MarkdownPane {
            public FileTabPane(String resource) {
                String textToShow;
                try(InputStream is = Util.getAssets("/assets/about/" + resource)) {
                    String content = IOUtils.toString(is, StandardCharsets.UTF_8);
                    textToShow = GUIConfiguration.markdownToHtml(content);
                } catch (IOException e) {
                    PWGUI.LOGGER.error("", e);
                    textToShow = String.format("Error trying to read file: %s", e.getMessage());
                }
                setInitialContent(textToShow);
            }
        }
    }
}