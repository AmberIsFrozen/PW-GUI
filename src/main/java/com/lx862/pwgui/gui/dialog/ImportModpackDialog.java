package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.executable.BatchedProgramExecution;
import com.lx862.pwgui.gui.components.AlignedBoxPanel;
import com.lx862.pwgui.gui.components.IconNamePairListCellRenderer;
import com.lx862.pwgui.gui.components.ComboBoxCardLinker;
import com.lx862.pwgui.gui.components.filepicker.ModrinthModpackFilter;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.frame.EditFrame;
import com.lx862.pwgui.gui.prompt.BatchedExecutionProgressDialog;
import com.lx862.pwgui.support.mrpack.ModpackFileEntry;
import com.lx862.pwgui.support.mrpack.ModpackIndex;
import com.lx862.pwgui.support.mrpack.ModrinthModpack;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.support.packwiz.data.IconNamePair;
import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.data.PackComponentVersion;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.components.filepicker.CurseForgeModpackFilter;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportModpackDialog extends BaseDialog {

    public ImportModpackDialog(Window owner, String title) {
        super(owner, title);
    }

    public static class ImportModpackPanel extends JPanel {
        private static final IconNamePair cf = new IconNamePair("CurseForge (.zip)", IconNamePair.CURSEFORGE.image());
        private static final IconNamePair mr = new IconNamePair("Modrinth (.mrpack)", IconNamePair.MODRINTH.image());

        public ImportModpackPanel(Window parent, Consumer<Path> importCallback) {
            setBorder(GUIConfiguration.getPaddedBorder(8, 0, 0, 0));
            setLayout(new BorderLayout());

            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));
            JPanel innerPanel = new KInlinePanel(FlowLayout.LEFT, 6, 0);

            innerPanel.add(new JLabel("Pack format:"));

            JPanel mainPanel = new JPanel(new CardLayout());

            JComboBox<IconNamePair> packFormatComboBox = new JComboBox<>();
            packFormatComboBox.setRenderer(new IconNamePairListCellRenderer());

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
                String authorName = Config.getInstance().authorName.getValue();
                if(authorName == null) authorName = "PW-GUI";

                NewModpackDialog.createModpack(parent, pack.index.name, authorName, pack.index.versionId, minecraft, modloader, (destination) -> {
                    PackwizExecutable.INSTANCE.changeWorkingDirectory(destination);

                    BatchedProgramExecution bpe = new BatchedProgramExecution();
                    for(ModpackFileEntry fileEntry : pack.index.files) {
                        bpe.add(generateModrinthCommand(destination, fileEntry));
                    }

                    bpe.onExit(success -> {
                        if(success) {
                            callback.accept(destination);
                        }
                    });

                    new BatchedExecutionProgressDialog(parent, "Importing Metadata...", Strings.REASON_TRIGGERED_BY_USER, bpe).setVisible(true);
                });
            }

            private ProgramExecution generateModrinthCommand(Path modpackDir, ModpackFileEntry fileEntry) {
                Path parentDir = modpackDir.resolve(fileEntry.path).getParent();
                String metaFolder = parentDir.equals(modpackDir) ? "./" : modpackDir.relativize(parentDir).toString();
                ProgramExecution pe;

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
                ProgramExecution programExecution = PackwizExecutable.INSTANCE.curseForge().importPack(sourceFile.toString()).build();
                programExecution.onExit(exitCode -> {
                    if(exitCode == 0) {
                        callback.run();
                    }
                });

                new TaskProgressDialog(parent, "Importing Modpack...", Strings.REASON_TRIGGERED_BY_USER, programExecution).setVisible(true);
            }
        }
    }
}
