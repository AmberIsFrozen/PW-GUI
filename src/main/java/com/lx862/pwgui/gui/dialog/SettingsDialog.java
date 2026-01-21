package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.BuildMetadata;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.core.data.ApplicationTheme;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.gui.action.DownloadPackwizAction;
import com.lx862.pwgui.gui.action.LocatePackwizAction;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

public class SettingsDialog extends BaseDialog {
    private final ProgramPanel programPanel;
    private final PackwizPanel packwizPanel;
    private final Config config;

    private final ApplicationTheme initialTheme;

    private boolean saved;

    public SettingsDialog(Config config, Window parent) {
        super(parent, Util.withTitlePrefix("Settings"), ModalityType.DOCUMENT_MODAL);
        this.config = PWGUI.getConfig();

        this.initialTheme = config.applicationTheme.getValue();

        setSize(400, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        KGridBagLayoutPanel settingsPanel = new KGridBagLayoutPanel(0, 1);

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setBorder(GUIHelper.getPaddedBorder(0, 0, 10, 0)); // Bottom padding to compensate
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        this.programPanel = new ProgramPanel(config, parent);
        this.programPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(programPanel);

        packwizPanel = new PackwizPanel();
        settingsPanel.add(packwizPanel);

        settingsPanel.addRow(1, programPanel);
        settingsPanel.addRow(1, packwizPanel);
        settingsPanel.addVerticalFiller();

        contentPanel.add(settingsPanel, BorderLayout.CENTER);

        KButton saveButton = new KButton("Save");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.addActionListener(actionEvent -> save());

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(saveButton).build();
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    private void save() {
        this.saved = true;
        programPanel.save();
        packwizPanel.save();
        Executables.packwiz.updateExecutableLocation(null);

        try {
            config.write(Strings.REASON_TRIGGERED_BY_USER);
            dispose();
        } catch (IOException e) {
            PWGUI.LOGGER.exception(e);
            JOptionPane.showMessageDialog(this, String.format("Failed to save config:\n%s", e.getMessage()), Util.withTitlePrefix("Config Saving Failed!"), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if(!saved) {
            GUIHelper.setupApplicationTheme(initialTheme, config.useWindowDecoration.getValue(), null);
        } else {
            programPanel.applyTheme();
        }
        super.dispose();
    }

    class ProgramPanel extends JPanel {
        private final JCheckBox relaunchModpackCheckbox;
        private final JCheckBox debugModeCheckBox;
        private final JCheckBox showPackwizMetaFileNameCheckbox;
        private final KComboBox<ApplicationTheme> themeComboBox;

        private final AuthorNamePanel authorNamePanel;

        public ProgramPanel(Config config, Window windowParent) {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setBorder(BorderFactory.createTitledBorder(BuildMetadata.INSTANCE.name));

            JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            themePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            themePanel.add(new JLabel("Theme:"));

            this.themeComboBox = new KComboBox<>();
            this.themeComboBox.setRenderer(new KListCellRenderer());
            for(ApplicationTheme applicationTheme : ApplicationTheme.values()) {
                themeComboBox.addItem(applicationTheme);
            }
            themeComboBox.setSelectedItem(initialTheme);

            themeComboBox.addItemListener(actionEvent -> {
                ApplicationTheme t = (ApplicationTheme)themeComboBox.getSelectedItem();
                GUIHelper.setupApplicationTheme(t, config.useWindowDecoration.getValue(), SettingsDialog.this);
            });
            themePanel.add(themeComboBox);
            add(themePanel);

            add(GUIHelper.createVerticalPadding(4));

            this.authorNamePanel = new AuthorNamePanel();
            authorNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(authorNamePanel);

            add(GUIHelper.createVerticalPadding(4));

            relaunchModpackCheckbox = new JCheckBox("Open last modpack on launch");
            relaunchModpackCheckbox.setSelected(config.openLastModpackOnLaunch.getValue());
            this.relaunchModpackCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(relaunchModpackCheckbox);

            showPackwizMetaFileNameCheckbox = new JCheckBox("Show packwiz metafile name (.pw.toml)");
            showPackwizMetaFileNameCheckbox.setSelected(config.showMetaFileName.getValue());
            this.showPackwizMetaFileNameCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(showPackwizMetaFileNameCheckbox);

            this.debugModeCheckBox = new JCheckBox("Enable Debug Log");
            debugModeCheckBox.setSelected(config.debugMode.getValue());
            debugModeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(debugModeCheckBox);

            add(GUIHelper.createVerticalPadding(4));

            KButton resetButton = new KButton(new ResetProgramAction(SettingsDialog.this, windowParent));
            resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(resetButton);
        }

        public void save() {
            config.applicationTheme.setValue((ApplicationTheme) themeComboBox.getSelectedItem());
            config.openLastModpackOnLaunch.setValue(relaunchModpackCheckbox.isSelected());
            config.debugMode.setValue(debugModeCheckBox.isSelected());
            config.showMetaFileName.setValue(showPackwizMetaFileNameCheckbox.isSelected());
            authorNamePanel.save();
        }

        public void applyTheme() {
            GUIHelper.setupApplicationTheme((ApplicationTheme) themeComboBox.getSelectedItem(), config.useWindowDecoration.getValue(), null);
        }

        class AuthorNamePanel extends KGridBagLayoutPanel {
            private final KTextField nameTextField;

            public AuthorNamePanel() {
                super(6, 3, 3);
                this.nameTextField = new KTextField("Name here");
                if(config.authorName.getValue() != null) this.nameTextField.setText(config.authorName.getValue());
                addRow(1, 1, new JLabel("Author pack as:"), this.nameTextField, new KHelpButton("This field is used to automatically fill the \"author\" field when creating a modpack, as well as the name field when changing the LICENSE file."));
                setAlignmentX(Component.LEFT_ALIGNMENT);
            }

            public void save() {
                config.authorName.setValue(this.nameTextField.getText().isEmpty() ? null : this.nameTextField.getText());
            }
        }
    }

    class PackwizPanel extends JPanel {
        private final JLabel packwizLocationLabel;
        private Path packwizLocationPath;

        public PackwizPanel() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setBorder(BorderFactory.createTitledBorder("Packwiz"));

            KGridBagLayoutPanel packwizLocationPanel = new KGridBagLayoutPanel(3, 2);
            packwizLocationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.packwizLocationLabel = new JLabel("Location: ???");

            KButton changePackwizLocationButton = new KButton(new LocatePackwizAction("Change...", SettingsDialog.this, () -> {
                Path newPath = PWGUI.getConfig().packwizExecutablePath.getValue();
                packwizLocationLabel.setText(String.format("Location: %s", newPath.toString()));
                packwizLocationLabel.setToolTipText(newPath.toString());
            }));

            packwizLocationPanel.addRow(1, 0, packwizLocationLabel, changePackwizLocationButton);

            add(packwizLocationPanel);

            KButton downloadPackwizButton = new KButton(new DownloadPackwizAction("Re-download packwiz", SettingsDialog.this, (path) -> {
                updatePackwizPath(path);
                JOptionPane.showMessageDialog(this, "Packwiz has been downloaded!", Util.withTitlePrefix("Packwiz Downloaded!"), JOptionPane.INFORMATION_MESSAGE);
            }));

            downloadPackwizButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(downloadPackwizButton);

            updatePackwizPath(config.packwizExecutablePath.getValue());
        }

        private void updatePackwizPath(Path newPath) {
            Path oldPath = config.lastModpackPath.getValue();

            config.lastModpackPath.setValue(newPath);
            boolean located = Executables.packwiz.probe(null) != null;
            config.lastModpackPath.setValue(oldPath); // restore

            if(!located) {
                JOptionPane.showMessageDialog(this, "The specified packwiz executable is not valid!", Util.withTitlePrefix("Invalid Executable"), JOptionPane.ERROR_MESSAGE);
            } else {
                packwizLocationLabel.setText(String.format("Location: %s", newPath.toString()));
                packwizLocationLabel.setToolTipText(newPath.toString());
                this.packwizLocationPath = newPath;
            }
        }

        public void save() {
            config.packwizExecutablePath.setValue(packwizLocationPath);
        }
    }

    static class ResetProgramAction extends AbstractAction {
        private final Window[] parents;

        public ResetProgramAction(Window... parents) {
            super("Reset...");
            this.parents = parents;
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(JOptionPane.showConfirmDialog(parents[0], String.format("This will reset %s to it's initial state as if it's the first time the program is launched.\nAre you sure you want to continue?", BuildMetadata.INSTANCE.name), Util.withTitlePrefix("Reset Program?"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                PWGUI.LOGGER.info("Resetting program!");

                try {
                    FileUtils.deleteDirectory(Config.CONFIG_DIR_PATH.toFile());
                } catch (IOException e) {
                    PWGUI.LOGGER.exception(e);
                    JOptionPane.showMessageDialog(parents[0], String.format("Failed to delete folder %s!\nCannot reset program!", Config.CONFIG_DIR_PATH), Util.withTitlePrefix("Reset Failed!"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for(Window parent : parents) {
                    parent.dispose();
                }
                PWGUI.init(null);
            }
        }
    }
}
