package com.lx862.pwgui.gui.frame;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.support.git.executable.GitExecutable;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.gui.action.SettingsAction;
import com.lx862.pwgui.gui.components.filepicker.PackFileFilter;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.gui.dialog.NewModpackDialog;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

/** The splash screen after packwiz executable is found */
public class WelcomeFrame extends BaseFrame {
    public WelcomeFrame(Component parent) {
        super(ApplicationInfo.INSTANCE.name);

        setSize(400, 525);
        setLocationRelativeTo(parent);

        PackwizExecutable.INSTANCE.setPackFileLocation(null);
        Config.getInstance().setLastModpackPath(null);
        jMenuBar.add(super.getViewMenu());
        jMenuBar.add(super.getHelpMenu());

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        JPanel mainPanel = new MainPanel(this);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel);
    }

    @Override
    public void dispose() {
        super.dispose();
        PackwizExecutable.INSTANCE.dispose();
        GitExecutable.INSTANCE.dispose();
    }

    static class MainPanel extends JPanel {
        private static final int LOGO_SIZE = 250;

        public MainPanel(JFrame parent) {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            add(Box.createVerticalGlue());

            JLabel logoLabel = new JLabel(new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/logo.png"), LOGO_SIZE), "Application Logo"));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(logoLabel);

            JLabel versionLabel = new JLabel(String.format("Version %s", ApplicationInfo.INSTANCE.version));
            versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(versionLabel);

            add(GUIConfiguration.createVerticalPadding(16));

            KButton openPackButton = new KButton(new OpenModpackAction(parent));
            openPackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(openPackButton);

            add(GUIConfiguration.createVerticalPadding(8));

            KButton createPackButton = new KButton(new CreateModpackAction(parent));
            createPackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(createPackButton);

            add(GUIConfiguration.createVerticalPadding(8));

            KButton settingsButton = new KButton(new SettingsAction(parent));
            settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(settingsButton);

            add(Box.createVerticalGlue());
        }

        public static void openModpack(Path path, JFrame parent) {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                Modpack modpack = new Modpack(path);
                EditFrame editFrame = new EditFrame(parent, modpack);
                parent.dispose();
                editFrame.setVisible(true);
            } catch (Exception e) {
                parent.setCursor(Cursor.getDefaultCursor());
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(parent, String.format("Failed to open modpack:\n%s", e.getMessage()), Util.withTitlePrefix("Failed to open Modpack"), JOptionPane.ERROR_MESSAGE);
            }
        }

        static class CreateModpackAction extends AbstractAction {
            private final JFrame parent;

            public CreateModpackAction(JFrame parent) {
                super("New modpack...");
                this.parent = parent;
                putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            }

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new NewModpackDialog(parent, (path) -> MainPanel.openModpack(path, parent)).setVisible(true);
            }
        }

        static class OpenModpackAction extends AbstractAction {
            private final JFrame parent;

            public OpenModpackAction(JFrame parent) {
                super("Open modpack...");
                this.parent = parent;
                putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            }

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                KFileChooser fileChooser = new KFileChooser("open-modpack");
                fileChooser.setFileFilter(new PackFileFilter());
                if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                    openModpack(fileChooser.getSelectedFile().toPath(), parent);
                }
            }
        }
    }
}