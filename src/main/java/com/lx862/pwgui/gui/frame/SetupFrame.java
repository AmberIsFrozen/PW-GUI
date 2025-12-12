package com.lx862.pwgui.gui.frame;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.core.Constants;
import com.lx862.pwgui.gui.action.DownloadPackwizAction;
import com.lx862.pwgui.gui.action.LocatePackwizAction;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.components.kui.KSeparator;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static javax.swing.Action.MNEMONIC_KEY;

/** The greeting splash screen if the packwiz executable is not found */
public class SetupFrame extends BaseFrame {
    private static final int LOGO_SIZE = 200;

    public SetupFrame(Component parent) {
        super(String.format("Welcome to %s!", Constants.PROGRAM_NAME));

        setSize(400, 550);
        setLocationRelativeTo(parent);

        KRootContentPanel contentPanel = new KRootContentPanel(20);
        MainPanel mainPanel = new MainPanel(this);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel);
        this.jMenuBar.add(super.getHelpMenu());
    }

    static class MainPanel extends JPanel {
        public MainPanel(JFrame parent) {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            add(Box.createVerticalGlue());

            JLabel logoLabel = new JLabel(new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/logo.png"), LOGO_SIZE), String.format("%s Logo", Constants.PROGRAM_NAME)));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(logoLabel);

            add(GUIHelper.createVerticalPadding(8));

            JLabel titleLabel = new JLabel(String.format("Heya! Welcome to %s!", Constants.PROGRAM_NAME));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
            add(titleLabel);

            add(GUIHelper.createVerticalPadding(8));

            JLabel descriptionLabel = new JLabel(String.format("<html><div style=\"text-align:center\">%s aims to simplify modpack management by providing a GUI around the command-line program packwiz.</div></html>", Constants.PROGRAM_NAME), SwingConstants.CENTER);
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(descriptionLabel);

            add(GUIHelper.createVerticalPadding(10));

            add(new KSeparator());

            add(GUIHelper.createVerticalPadding(10));

            JLabel whatToDoNextDescriptionLabel = new JLabel("<html><p style=\"text-align:center\">Don't have packwiz yet? Just click the download button below and we'll take care of it!</p><p style=\"margin-top:7px;text-align:center;\">Otherwise, please give me a favor by pointing me to the packwiz executable~</p></html>", SwingConstants.CENTER);
            whatToDoNextDescriptionLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            whatToDoNextDescriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(whatToDoNextDescriptionLabel);
            add(Box.createRigidArea(new Dimension(0, 16)));

            KButton downloadButton = new KButton(new DownloadPackwizAction("Download Packwiz", parent, (path) -> {
                JOptionPane.showMessageDialog(parent, String.format("Packwiz has been downloaded and configured!\nPath: %s", path), Util.withTitlePrefix("Download Success!"), JOptionPane.INFORMATION_MESSAGE);
                WelcomeFrame welcomeFrame = new WelcomeFrame(parent);
                welcomeFrame.setVisible(true);
                parent.dispose();
            }));
            downloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(downloadButton);

            add(Box.createRigidArea(new Dimension(0, 8)));

            LocatePackwizAction action = new LocatePackwizAction("Locate Packwiz...", parent, () -> {
                JOptionPane.showMessageDialog(parent, "Packwiz executable has been configured!", Util.withTitlePrefix("Configure Success!"), JOptionPane.INFORMATION_MESSAGE);
                WelcomeFrame welcomeFrame = new WelcomeFrame(parent);
                welcomeFrame.setVisible(true);
                parent.dispose();
            });
            action.putValue(MNEMONIC_KEY, KeyEvent.VK_L);

            KButton locateButton = new KButton(action);
            locateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(locateButton);

            add(Box.createVerticalGlue());
        }
    }
}
