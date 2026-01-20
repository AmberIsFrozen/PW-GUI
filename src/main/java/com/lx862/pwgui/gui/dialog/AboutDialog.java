package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.github.rjeschke.txtmark.Processor;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Constants;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.components.kui.KTabbedPane;
import com.lx862.pwgui.gui.panel.editing.filetype.MarkdownPanel;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class AboutDialog extends BaseDialog {
    public AboutDialog(Window parent) {
        super(parent, String.format("About %s", Constants.PROGRAM_NAME), ModalityType.DOCUMENT_MODAL);
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

            JLabel logoLabel = new JLabel(new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/logo.png"), 200), String.format("%s Logo", Constants.PROGRAM_NAME)));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(logoLabel);

            JLabel titleLabel = new JLabel(Constants.PROGRAM_NAME);
            titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(titleLabel);

            JLabel descriptionLabel = new JLabel("<html><div style='text-align:center'>GUI Wrapper for packwiz, a tool to edit & distribute Minecraft modpacks.</div></html>", SwingConstants.CENTER);
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(descriptionLabel);

            JLabel versionLabel = new JLabel(Constants.VERSION);
            versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(versionLabel);

            add(GUIHelper.createVerticalPadding(8));

            KTabbedPane tabbedPane = new KTabbedPane();
            tabbedPane.addTab("External Links", new JScrollPane(new FileTabPane("links.md")));
            tabbedPane.addTab("Details", new JScrollPane(new FileTabPane("details.md")));
            tabbedPane.addTab("Credits", new JScrollPane(new FileTabPane("credits.md")));
            tabbedPane.addTab("Duke", new JScrollPane(new FileTabPane("duke.md")));

            add(tabbedPane);

            JLabel footerLabel = new JLabel(String.format("%s 2026 <3", Constants.AUTHOR));
            footerLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(footerLabel);
        }

        static class FileTabPane extends MarkdownPanel.MarkdownPane {
            public FileTabPane(String resource) {
                String textToShow;
                try {
                    textToShow = Processor.process(Util.getAssets("/assets/about/" + resource));
                } catch (IOException e) {
                    PWGUI.LOGGER.exception(e);
                    textToShow = String.format("Error trying to read file: %s", e.getMessage());
                }
                setInitialContent(textToShow);
            }
        }
    }
}