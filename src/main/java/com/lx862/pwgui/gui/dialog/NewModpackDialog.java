package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.gui.panel.ModpackInfoPanel;
import com.lx862.pwgui.gui.panel.ModpackVersionPanel;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;
import com.lx862.pwgui.executable.ProgramExecution;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class NewModpackDialog extends BaseDialog {
    private ModpackInfoPanel modpackInfoPanel = null;
    public NewModpackDialog(JFrame frame, Consumer<Path> packCreatedCallback) {
        super(frame, Util.withTitlePrefix("New Modpack"), true);

        setSize(400, 525);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new KRootContentPanel(10);

        JLabel titleLabel = new JLabel("New Modpack...");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setBorder(GUIConfiguration.getPaddedBorder(0, 0, 10, 0)); // Bottom padding to compensate
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane createImportTabPane = new JTabbedPane();

        JPanel createPanel = new JPanel(new BorderLayout());
        JPanel createFormPanel = new JPanel();
        createFormPanel.setLayout(new BoxLayout(createFormPanel, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel("Please fill the basic info about your (soon to be!) modpack.");
        descriptionLabel.setBorder(GUIConfiguration.getPaddedBorder(10));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        createFormPanel.add(descriptionLabel);

        KButton saveButton = new KButton("Create & Save!");

        modpackInfoPanel = new ModpackInfoPanel(null, () -> {
            saveButton.setEnabled(modpackInfoPanel.requiredInfoFilled());
        });
        saveButton.setEnabled(modpackInfoPanel.requiredInfoFilled());

        ModpackVersionPanel modpackVersionPanel = new ModpackVersionPanel(null, () -> {});
        createFormPanel.add(modpackInfoPanel);
        createFormPanel.add(new KSeparator());
        createFormPanel.add(modpackVersionPanel);
        createFormPanel.add(Box.createVerticalGlue());
        createFormPanel.add(new KSeparator());
        createPanel.add(createFormPanel, BorderLayout.CENTER);

        saveButton.addActionListener(actionEvent -> {
            createModpack(modpackInfoPanel, modpackVersionPanel, (path) -> {
                doAftermath(path, modpackVersionPanel.getModloader() != null);
                packCreatedCallback.accept(path.resolve("pack.toml"));
            });
        });

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(saveButton).build();
        createPanel.add(actionPanel, BorderLayout.SOUTH);

        createImportTabPane.add("Create", createPanel);
        createImportTabPane.add("Import", new ImportModpackDialog.ImportModpackPanel(this, true, packCreatedCallback));
        contentPanel.add(createImportTabPane, BorderLayout.CENTER);

        add(contentPanel);
    }

    private void createModpack(ModpackInfoPanel modpackInfoPanel, ModpackVersionPanel modpackVersionPanel, Consumer<Path> finishCallback) {
        try {
            KFileChooser fileChooser = new KFileChooser("new-modpack");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Choose a folder to store your modpack in...");

            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                String cleanFilesystemName = modpackInfoPanel.getModpackName().replaceAll("(?U)[^\\w\\._]+", "_"); // https://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars
                File modpackDirectory = directory.toPath().resolve(cleanFilesystemName).toFile();

                if(Files.exists(modpackDirectory.toPath())) {
                    if(modpackDirectory.isFile()) {
                        JOptionPane.showMessageDialog(this, String.format("A file with name \"%s\" already exists!\nConsider removing/renaming the file.", modpackDirectory.getName()), Util.withTitlePrefix("Create Modpack"), JOptionPane.ERROR_MESSAGE);
                        createModpack(modpackInfoPanel, modpackVersionPanel, finishCallback);
                        return;
                    } else if(modpackDirectory.list().length > 0) {
                        // Note: We use "folder already exists" because it's easier to get by. If a user have an empty directory, this won't prompt because no data will be loss.
                        if(JOptionPane.showConfirmDialog(this, String.format("\"%s\" folder already exists!\nDo you want to use that folder for your Modpack anyway?\nWARNING: This would remove EVERYTHING within the folder!", modpackDirectory.getName()), Util.withTitlePrefix("Create Modpack"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            FileUtils.deleteDirectory(modpackDirectory);
                            Files.createDirectory(modpackDirectory.toPath());
                        } else {
                            createModpack(modpackInfoPanel, modpackVersionPanel, finishCallback);
                            return;
                        }
                    }
                } else {
                    Files.createDirectory(modpackDirectory.toPath());
                }

                // Build arguments
                PackwizExecutable.PackwizArgumentBuilder arguments = PackwizExecutable.INSTANCE.init();
                arguments.append(modpackInfoPanel.getInitArguments());
                arguments.append(modpackVersionPanel.getInitArguments());

                PackwizExecutable.INSTANCE.changeWorkingDirectory(modpackDirectory.toPath());

                ProgramExecution processExecution = arguments.build();
                processExecution.onExit(exitCode -> {
                    if(exitCode == 0) {
                        dispose();
                        if(finishCallback != null) finishCallback.accept(modpackDirectory.toPath());
                    }
                });

                new TaskProgressDialog(this, "Creating Modpack...", Strings.REASON_TRIGGERED_BY_USER, processExecution).setVisible(true);
            }
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(this, String.format("Failed to create modpack:\n%s", e.getMessage()), Util.withTitlePrefix("Create Modpack"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /* We also add several files/directory on top of packwiz by default */
    private void doAftermath(Path path, boolean isModded) {
        if(isModded) {
            path.resolve("config").toFile().mkdir();
            path.resolve("mods").toFile().mkdir();
        }

        try {
            Util.copyAssetsToPath("/assets/new_modpack_structure/README.md", "README.md", path);
            Util.copyAssetsToPath("/assets/new_modpack_structure/gitattributes", ".gitattributes", path);
            Util.copyAssetsToPath("/assets/new_modpack_structure/gitignore", ".gitignore", path);
            Util.copyAssetsToPath("/assets/new_modpack_structure/_LICENSE", "LICENSE", path);
            Util.copyAssetsToPath("/assets/new_modpack_structure/.packwizignore", ".packwizignore", path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to copy additional files to the modpack.\nYou may need to manually create the \"mods\" and \"config\" folder if you want to install mods.", Util.withTitlePrefix("Create Modpack"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
