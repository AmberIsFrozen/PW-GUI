package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.task.BatchedTask;
import com.lx862.pwgui.gui.components.AlignedBoxPanel;
import com.lx862.pwgui.gui.components.ComboBoxCardLinker;
import com.lx862.pwgui.gui.components.IconNamePairListCellRenderer;
import com.lx862.pwgui.gui.components.filepicker.CurseForgeModpackFilter;
import com.lx862.pwgui.gui.components.filepicker.ModrinthModpackFilter;
import com.lx862.pwgui.support.mrpack.ModpackFileEntry;
import com.lx862.pwgui.support.mrpack.ModpackIndex;
import com.lx862.pwgui.support.mrpack.ModrinthModpack;
import com.lx862.pwgui.support.packwiz.data.IconNamePair;
import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.data.PackComponentVersion;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.gui.panel.ModpackInfoPanel;
import com.lx862.pwgui.gui.panel.ModpackVersionPanel;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.task.RunProgramTask;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            createModpack(
                this,
                modpackInfoPanel.getPackName(), modpackInfoPanel.getPackAuthor(), modpackInfoPanel.getPackVersion(),
                modpackVersionPanel.getMinecraft(), modpackVersionPanel.getModloader(),
                (path) -> {
                    dispose();
                    doAftermath(path, modpackVersionPanel.getModloader() != null);
                    packCreatedCallback.accept(path.resolve("pack.toml"));
                }
            );
        });

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(saveButton).build();
        createPanel.add(actionPanel, BorderLayout.SOUTH);

        createImportTabPane.add("Create", createPanel);
        createImportTabPane.add("Import", new ImportModpackPanel(this, packCreatedCallback));
        contentPanel.add(createImportTabPane, BorderLayout.CENTER);

        add(contentPanel);
    }

    public static void createModpack(Window parent, String packName, String packAuthor, String packVersion, PackComponentVersion minecraft, PackComponentVersion modloader, Consumer<Path> finishCallback) {
        try {
            KFileChooser fileChooser = new KFileChooser("new-modpack");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Choose a folder to store your modpack in...");

            fileChooser.setApproveCallback((selected, ctx) -> {
                File firstSelected = selected[0];
                if(firstSelected.list().length > 0) {
                    ctx.showMessageDialog(JOptionPane.INFORMATION_MESSAGE, "Folder not empty", "The selected folder is not empty, please create an empty folder for the modpack to be stored in.", JOptionPane.OK_OPTION);
                    return SystemFileChooser.CANCEL_OPTION;
                } else {
                    return SystemFileChooser.APPROVE_OPTION;
                }
            });

            if(fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File modpackDirectory = fileChooser.getSelectedFile();

                // Build arguments
                PackwizExecutable.PackwizArgumentBuilder arguments = PackwizExecutable.INSTANCE.init(packName, packAuthor, packVersion, minecraft.getVersion(), modloader);

                PackwizExecutable.INSTANCE.changeWorkingDirectory(modpackDirectory.toPath());

                RunProgramTask processExecution = arguments.build();
                processExecution.onExit(exitResult -> {
                    if(exitResult.success()) {
                        if(finishCallback != null) finishCallback.accept(modpackDirectory.toPath());
                    }
                });

                TaskDialog taskDialog = new TaskDialog(parent, "Creating Modpack...", processExecution);
                processExecution.run(Strings.REASON_TRIGGERED_BY_USER);
                taskDialog.setVisible(true);
            }
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(parent, String.format("Failed to create modpack:\n%s", e.getMessage()), Util.withTitlePrefix("Create Modpack"), JOptionPane.ERROR_MESSAGE);
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

class ImportModpackPanel extends JPanel {
    private static final IconNamePair cf = new IconNamePair("CurseForge (.zip)", IconNamePair.CURSEFORGE.image());
    private static final IconNamePair mr = new IconNamePair("Modrinth (.mrpack)", IconNamePair.MODRINTH.image());

    public ImportModpackPanel(Window parent, Consumer<Path> importCallback) {
        setBorder(GUIConfiguration.getPaddedBorder(8, 0, 0, 0));
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));
        JPanel innerPanel = new KInlinePanel(FlowLayout.LEFT, 6, 0);

        innerPanel.add(new JLabel("Pack format:"));

        JComboBox<IconNamePair> packFormatComboBox = new JComboBox<>();
        packFormatComboBox.setRenderer(new IconNamePairListCellRenderer());

        JPanel mainPanel = new JPanel(new CardLayout());
        ComboBoxCardLinker<IconNamePair> comboBoxCardLinker = new ComboBoxCardLinker<>(packFormatComboBox, mainPanel);
        comboBoxCardLinker.addTab(mr, new ImportModrinthPanel(parent, importCallback));
        comboBoxCardLinker.addTab(cf, new ImportCurseForgePanel(parent, importCallback));
        comboBoxCardLinker.apply();

        innerPanel.add(packFormatComboBox);
        headerPanel.add(innerPanel);
        headerPanel.add(GUIConfiguration.createVerticalPadding(6));
        headerPanel.add(new JSeparator());

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    static class ImportModrinthPanel extends AlignedBoxPanel {
        private final Window parent;
        public ImportModrinthPanel(Window parent, Consumer<Path> importCallback) {
            super(LEFT_ALIGNMENT);
            this.parent = parent;

            AtomicReference<ModrinthModpack> selectedPack = new AtomicReference<>();

            JButton importButton = new JButton("Import!");
            importButton.setEnabled(false);

            JLabel descriptionLabel = new JLabel("Work-in-progress!");
            add(descriptionLabel);

            add(GUIConfiguration.createVerticalPadding(10));

            JPanel pathPanel = new KInlinePanel();
            pathPanel.add(new JLabel("Selected path:"));
            JLabel pathLabel = new JLabel("None");
            pathLabel.setFont(pathLabel.getFont().deriveFont(Font.BOLD));
            pathPanel.add(pathLabel);
            add(pathPanel);

            JButton selectPackButton = new JButton("Select pack...");
            selectPackButton.addActionListener(e -> {
                KFileChooser kFileChooser = new KFileChooser("import-modpack-source");
                kFileChooser.setFileFilter(new ModrinthModpackFilter());

                if(kFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = kFileChooser.getSelectedFile();

                    try {
                        selectedPack.set(new ModrinthModpack(file.toPath()));
                        pathLabel.setText(file.getAbsoluteFile().toString());
                        importButton.setEnabled(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "The selected pack is invalid!\n" + ex.getMessage(), "Invalid Modrinth Modpack!", JOptionPane.ERROR_MESSAGE);
                        PWGUI.LOGGER.error("Failed to parse modrinth modpack!", ex);
                    }
                }
            });

            add(selectPackButton);
            add(Box.createVerticalGlue());
            add(new KSeparator());

            importButton.addActionListener(e -> {
                doImport(selectedPack.get(), (path) -> {
                    importCallback.accept(path.resolve("pack.toml"));
                });
            });

            KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(importButton).build();
            add(actionPanel);
        }

        private void doImport(ModrinthModpack pack, Consumer<Path> callback) {
            PackComponentVersion minecraft = null;
            PackComponentVersion modloader = null;

            for(Map.Entry<String, String> dependency : pack.index.dependencies.entrySet()) {
                String dependencyName = dependency.getKey();
                String dependencyVersion = dependency.getValue();

                switch(dependencyName) {
                    case ModpackIndex.DEP_MINECRAFT -> {
                        minecraft = new PackComponentVersion(PackComponent.MINECRAFT, dependencyVersion);
                    }
                    case ModpackIndex.DEP_FABRIC -> {
                        if(modloader != null) throw new IllegalArgumentException("Multiple modloaders are not supported!");
                        modloader = new PackComponentVersion(PackComponent.FABRIC, dependencyVersion);
                    }
                    case ModpackIndex.DEP_FORGE -> {
                        if(modloader != null) throw new IllegalArgumentException("Multiple modloaders are not supported!");
                        modloader = new PackComponentVersion(PackComponent.FORGE, dependencyVersion);
                    }
                    case ModpackIndex.DEP_QUILT -> {
                        if(modloader != null) throw new IllegalArgumentException("Multiple modloaders are not supported!");
                        modloader = new PackComponentVersion(PackComponent.QUILT, dependencyVersion);
                    }
                    case ModpackIndex.DEP_NEOFORGE -> {
                        if(modloader != null) throw new IllegalArgumentException("Multiple modloaders are not supported!");
                        modloader = new PackComponentVersion(PackComponent.NEOFORGE, dependencyVersion);
                    }
                }
            }

            String authorName = Config.getInstance().authorName.valueOr("PW-GUI");

            NewModpackDialog.createModpack(parent, pack.index.name, authorName, pack.index.versionId, minecraft, modloader, (destination) -> {
                PackwizExecutable.INSTANCE.changeWorkingDirectory(destination);

                BatchedTask batchedTask = new BatchedTask("packwiz");
                for(ModpackFileEntry fileEntry : pack.index.files) {
                    batchedTask.add(getCliAddCommand(destination, fileEntry));
                }

                batchedTask.onExit(exitResult -> {
                    if(exitResult.success()) {
                        callback.accept(destination);
                    }
                });

                TaskDialog taskDialog = new TaskDialog(parent, "Importing Pack...", batchedTask);
                batchedTask.run(Strings.REASON_TRIGGERED_BY_USER);
                taskDialog.setVisible(true);
            });
        }

        private RunProgramTask getCliAddCommand(Path modpackDir, ModpackFileEntry fileEntry) {
            Path parentDir = modpackDir.resolve(fileEntry.path).getParent();
            String metaFolder = parentDir.equals(modpackDir) ? "./" : modpackDir.relativize(parentDir).toString();
            RunProgramTask pe;

            URI downloadURL = fileEntry.downloads[0];
            if(downloadURL != null) {
                // HACK: We are parsing the CDN URL for now, this is a terrible idea as they aren't consistent, we should replace them with the hash checking API before this gets into prod.
                if(downloadURL.getHost().contains("cdn.modrinth.com")) {

                    String cdnVersionName = downloadURL.toString().split("versions/")[1].split("/")[0];
                    if(cdnVersionName.contains(".")) { // Some version file on the cdn use the version name directly instead of id
                        String projectId = downloadURL.toString().split("data/")[1].split("/")[0];
                        pe = PackwizExecutable.INSTANCE.modrinth().add(projectId).metaFolder(metaFolder).build();
                    } else {
                        pe = PackwizExecutable.INSTANCE.modrinth().add(downloadURL.toString()).metaFolder(metaFolder).build();
                    }
                } else {
                    String potentialSlug = Path.of(fileEntry.path).getFileName().toString();
                    Matcher matcher = Pattern.compile("-[0-9]").matcher(potentialSlug);
                    if(matcher.find()) {
                        potentialSlug = potentialSlug.substring(0, matcher.start());
                    }

                    pe = PackwizExecutable.INSTANCE.url().add(potentialSlug, downloadURL.toString(), true).metaFolder(metaFolder).build();
                }
            } else {
                pe = null;
            }

            pe.onOutput(outputMessage -> {
                if(outputMessage.isPrompt()) {
                    pe.enterInput("N");
                }
            });
            return pe;
        }
    }

    static class ImportCurseForgePanel extends AlignedBoxPanel {
        private final Window parent;
        private Path pathLocation = null;

        public ImportCurseForgePanel(Window parent, Consumer<Path> importCallback) {
            super(LEFT_ALIGNMENT);

            this.parent = parent;
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel description = new JLabel("<html>" +
                    "<p>You can click the import button below to create a new packwiz modpack from a pre-existing CurseForge modpack.</p>" +
                    "</html>");
            add(description);

            add(GUIConfiguration.createVerticalPadding(10));

            JPanel pathPanel = new KInlinePanel();
            pathPanel.add(new JLabel("Selected path:"));
            JLabel pathLabel = new JLabel("None");
            pathLabel.setFont(pathLabel.getFont().deriveFont(Font.BOLD));
            pathPanel.add(pathLabel);
            add(pathPanel);

            KButton importButton = new KButton("Import...");
            importButton.setEnabled(false);
            importButton.addActionListener(actionEvent -> importCurseForgeModpack(pathLocation, importCallback));

            JButton selectPackButton = new JButton("Select pack...");
            selectPackButton.addActionListener(e -> {
                KFileChooser kFileChooser = new KFileChooser("import-modpack-source");
                kFileChooser.setFileFilter(new CurseForgeModpackFilter());

                if(kFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = kFileChooser.getSelectedFile();
                    pathLabel.setText(file.getAbsoluteFile().toString());
                    pathLocation = file.toPath();
                    importButton.setEnabled(true);
                }
            });

            add(selectPackButton);

            add(Box.createVerticalGlue());
            add(new KSeparator());

            KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(importButton).build();
            add(actionPanel);
        }

        private void importCurseForgeModpack(Path packLocation, Consumer<Path> importCallback) {
            File file = packLocation.toFile();

            KFileChooser modpackFileChooser = new KFileChooser("import-modpack-destination");
            modpackFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(modpackFileChooser.openSaveDirectoryDialog(this) == JFileChooser.APPROVE_OPTION) {
                File destinationPath = modpackFileChooser.getSelectedFile();
                PackwizExecutable.INSTANCE.changeWorkingDirectory(destinationPath.toPath());

                runCurseForgeImportCommand(file, () -> {
                    importCallback.accept(destinationPath.toPath().resolve("pack.toml"));
                });
            }
        }

        private void runCurseForgeImportCommand(File sourceFile, Runnable callback) {
            RunProgramTask runProgramTask = PackwizExecutable.INSTANCE.curseForge().importPack(sourceFile.toString()).build();
            runProgramTask.onExit(exitResult -> {
                if(exitResult.success()) {
                    callback.run();
                }
            });

            TaskDialog taskDialog = new TaskDialog(parent, "Importing Modpack...", runProgramTask);
            runProgramTask.run(Strings.REASON_TRIGGERED_BY_USER);
            taskDialog.setVisible(true);
        }
    }
}
