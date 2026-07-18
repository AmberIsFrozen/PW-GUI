package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemFileChooser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.task.*;
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
import com.lx862.pwgui.util.NetworkHelper;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.gui.panel.ModpackInfoPanel;
import com.lx862.pwgui.gui.panel.ModpackVersionPanel;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;
import org.zeroturnaround.zip.ZipUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
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
            try {
                AtomicReference<Path> packPath = new AtomicReference<>();
                Task task = createModpack(
                        this,
                        modpackInfoPanel.getPackName(), modpackInfoPanel.getPackAuthor(), modpackInfoPanel.getPackVersion(),
                        modpackVersionPanel.getMinecraft(), modpackVersionPanel.getModloader(),
                        (path) -> {
                            packPath.set(path);
                            PackwizExecutable.INSTANCE.changeWorkingDirectory(path);
                        }
                );

                if(task != null) {
                    task.onExit(exitResult -> {
                        if(exitResult.success()) {
                            dispose();
                            doAftermath(packPath.get(), modpackVersionPanel.getModloader() != null);
                            packCreatedCallback.accept(packPath.get().resolve("pack.toml"));
                        }
                    });

                    TaskDialog taskDialog = new TaskDialog(this, "Creating Modpack...", task);
                    task.run(Strings.REASON_TRIGGERED_BY_USER);
                    taskDialog.setVisible(true);
                }
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(this, String.format("Failed to create modpack:\n%s", e.getMessage()), Util.withTitlePrefix("Create Modpack"), JOptionPane.ERROR_MESSAGE);
            }
        });

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(saveButton).build();
        createPanel.add(actionPanel, BorderLayout.SOUTH);

        createImportTabPane.add("Create", createPanel);
        createImportTabPane.add("Import", new ImportModpackPanel(this, packCreatedCallback));
        contentPanel.add(createImportTabPane, BorderLayout.CENTER);

        add(contentPanel);
    }

    public static Task createModpack(Window parent, String packName, String packAuthor, String packVersion, PackComponentVersion minecraft, PackComponentVersion modloader, Consumer<Path> pathCallback) {
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
            pathCallback.accept(modpackDirectory.toPath());

            // Build arguments
            PackwizExecutable.PackwizArgumentBuilder arguments = PackwizExecutable.INSTANCE.init(packName, packAuthor, packVersion, minecraft.getVersion(), modloader);
            return arguments.build();
        } else {
            return null;
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
        private Sideness importSide = Sideness.CLIENT;

        public ImportModrinthPanel(Window parent, Consumer<Path> importCallback) {
            super(LEFT_ALIGNMENT);
            this.parent = parent;

            AtomicReference<ModrinthModpack> selectedPack = new AtomicReference<>();

            JButton importButton = new JButton("Import!");
            importButton.setEnabled(false);

            JLabel descriptionLabel = new JLabel("<html>You may import a <b>Modrinth Modpack (.mrpack)</b> and create a packwiz-formatted pack out of it.<br>Note that this is an additional feature added to PW-GUI and not a part of packwiz.</html>");
            add(descriptionLabel);

            add(GUIConfiguration.createVerticalPadding(5));

            add(new KSeparator());

            add(GUIConfiguration.createVerticalPadding(5));

            JPanel modpackNamePanel = new KInlinePanel();
            modpackNamePanel.add(new JLabel("Modpack Name:"));
            JLabel modpackNameLabel = new JLabel("None");
            modpackNameLabel.setFont(modpackNameLabel.getFont().deriveFont(Font.BOLD));
            modpackNamePanel.add(modpackNameLabel);
            modpackNamePanel.setVisible(false);
            add(modpackNamePanel);

            JPanel modpackVersionPanel = new KInlinePanel();
            modpackVersionPanel.add(new JLabel("Modpack Version:"));
            JLabel modpackVersionLabel = new JLabel("None");
            modpackVersionLabel.setFont(modpackVersionLabel.getFont().deriveFont(Font.BOLD));
            modpackVersionPanel.add(modpackVersionLabel);
            modpackVersionPanel.setVisible(false);
            add(modpackVersionPanel);

            JPanel modpackPathPanel = new KInlinePanel();
            modpackPathPanel.add(new JLabel("Selected path:"));
            JLabel modpackPathLabel = new JLabel("None");
            modpackPathLabel.setFont(modpackPathLabel.getFont().deriveFont(Font.BOLD));
            modpackPathPanel.add(modpackPathLabel);
            modpackPathPanel.setVisible(false);
            add(modpackPathPanel);

            JButton selectPackButton = new JButton("Select pack...");
            selectPackButton.addActionListener(e -> {
                KFileChooser kFileChooser = new KFileChooser("import-modpack-source");
                kFileChooser.setFileFilter(new ModrinthModpackFilter());

                if(kFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = kFileChooser.getSelectedFile();

                    try {
                        selectedPack.set(new ModrinthModpack(file.toPath()));
                        importButton.setEnabled(true);

                        modpackNameLabel.setText(selectedPack.get().index.name);
                        modpackNamePanel.setVisible(true);

                        modpackVersionLabel.setText(selectedPack.get().index.versionId);
                        modpackVersionPanel.setVisible(true);

                        modpackPathLabel.setText(file.getAbsoluteFile().toString());
                        modpackPathPanel.setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "The selected pack is invalid!\n" + ex.getMessage(), "Invalid Modrinth Modpack!", JOptionPane.ERROR_MESSAGE);
                        PWGUI.LOGGER.error("Failed to parse modrinth modpack!", ex);
                    }
                }
            });

            add(selectPackButton);

            JPanel importSettingsPanel = new AlignedBoxPanel(LEFT_ALIGNMENT);
            JPanel sidenessSettings = new KInlinePanel();
            sidenessSettings.add(new JLabel("Side:"));

            JRadioButton clientRadio = (JRadioButton) sidenessSettings.add(new JRadioButton("Client"));
            JRadioButton serverRadio = (JRadioButton) sidenessSettings.add(new JRadioButton("Server"));
            JRadioButton bothRadio = (JRadioButton) sidenessSettings.add(new JRadioButton("Both"));
            bothRadio.setSelected(true);

            clientRadio.addActionListener(e -> importSide = Sideness.CLIENT);
            serverRadio.addActionListener(e -> importSide = Sideness.SERVER);
            bothRadio.addActionListener(e -> importSide = Sideness.BOTH);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(clientRadio);
            buttonGroup.add(serverRadio);
            buttonGroup.add(bothRadio);

            importSettingsPanel.add(sidenessSettings);

            KCollapsibleToggle kCollapsibleToggle = new KCollapsibleToggle("Import Settings", "Import Settings", importSettingsPanel);
            add(kCollapsibleToggle);
            add(importSettingsPanel);
            add(Box.createVerticalGlue());
            add(new KSeparator());

            importButton.addActionListener(e -> {
                doImport(selectedPack.get(), importSide, (path) -> {
                    importCallback.accept(path.resolve("pack.toml"));
                });
            });

            KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(importButton).build();
            add(actionPanel);
        }

        private void doImport(ModrinthModpack pack, Sideness side, Consumer<Path> callback) {
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

            BatchedTask importPackTask = new BatchedTask("Pack Importer");
            AtomicReference<Path> packDestinationPath = new AtomicReference<>();

            Task createPackTask = NewModpackDialog.createModpack(parent, pack.index.name, Config.getInstance().authorName.valueOr("PW-GUI"), pack.index.versionId, minecraft, modloader, (destination) -> {
                PackwizExecutable.INSTANCE.changeWorkingDirectory(destination);
                packDestinationPath.set(destination);
            });
            importPackTask.add(createPackTask);

            /* Fetch MR for version */
            Map<String, ModrinthVersionEntry> projectVersions = new HashMap<>();

            JsonObject versionFilesRequestObject = new JsonObject();
            JsonArray hashArray = new JsonArray();

            for(ModpackFileEntry fileEntry : pack.index.files) {
                hashArray.add(fileEntry.hashes.sha512);
            }
            versionFilesRequestObject.add("hashes", hashArray);
            versionFilesRequestObject.addProperty("algorithm", "sha512");

            HttpFetchTask httpFetchTask = new HttpFetchTask("Fetching modrinth projects...", () -> {
                return NetworkHelper.postRequest("https://api.modrinth.com/v2/version_files", versionFilesRequestObject.toString().getBytes());
            }, (str) -> {
                JsonObject resultObject = new Gson().fromJson(str, JsonObject.class);
                for(Map.Entry<String, JsonElement> versionEntry : resultObject.entrySet()) {
                    JsonObject versionObject = versionEntry.getValue().getAsJsonObject();
                    ModrinthVersionEntry pv = new ModrinthVersionEntry(versionEntry.getKey(), versionObject.get("project_id").getAsString(), versionObject.get("id").getAsString());
                    projectVersions.put(versionEntry.getKey(), pv);
                }
            });

            importPackTask.add(httpFetchTask);

            /* Add copy override tasks */
            if(ZipUtil.containsEntry(pack.getFile(), "overrides")) {
                Task extractZipTask = new ExtractZipTask("Extracting files...", pack.getFile(), packDestinationPath.get().toFile(), new ExtractZipTask.InnerDirectory("overrides"));
                importPackTask.add(extractZipTask);
            }

            if(ZipUtil.containsEntry(pack.getFile(), "client-overrides") && (side == Sideness.CLIENT || side == Sideness.BOTH)) {
                Task extractZipTask = new ExtractZipTask("Extracting client files...", pack.getFile(), packDestinationPath.get().toFile(), new ExtractZipTask.InnerDirectory("client-overrides"));
                importPackTask.add(extractZipTask);
            }

            if(ZipUtil.containsEntry(pack.getFile(), "server-overrides") && (side == Sideness.SERVER || side == Sideness.BOTH)) {
                Task extractZipTask = new ExtractZipTask("Extracting server files...", pack.getFile(), packDestinationPath.get().toFile(), new ExtractZipTask.InnerDirectory("client-overrides"));
                importPackTask.add(extractZipTask);
            }

            importPackTask.onExit(exitResult -> {
                if(exitResult.success()) {
                    BatchedTask importMetadataTask = new BatchedTask("packwiz");

                    for(ModpackFileEntry fileEntry : pack.index.files) {
                        ModrinthVersionEntry pv = projectVersions.get(fileEntry.hashes.sha512);
                        importMetadataTask.add(getModrinthAddTask(packDestinationPath.get(), fileEntry, pv));
                    }

                    importMetadataTask.onExit(metadataExitResult -> {
                        if(metadataExitResult.success()) {
                            callback.accept(packDestinationPath.get());
                        }
                    });

                    TaskDialog taskDialog = new TaskDialog(parent, "Importing Metadata...", importMetadataTask);
                    importMetadataTask.run(Strings.REASON_TRIGGERED_BY_USER);
                    taskDialog.setVisible(true);
                }
            });

            TaskDialog taskDialog = new TaskDialog(parent, "Importing Pack...", importPackTask);
            importPackTask.run(Strings.REASON_TRIGGERED_BY_USER);
            taskDialog.setVisible(true);
        }

        private RunProgramTask getModrinthAddTask(Path modpackDir, ModpackFileEntry fileEntry, ModrinthVersionEntry modrinthVersionEntry) {
            Path parentDir = modpackDir.resolve(fileEntry.path).getParent();
            String metaFolder = parentDir.equals(modpackDir) ? "./" : modpackDir.relativize(parentDir).toString();
            RunProgramTask pe;

            URI downloadURL = fileEntry.downloads[0];
            if(downloadURL != null) {
                if(modrinthVersionEntry != null) {
                    pe = PackwizExecutable.INSTANCE.modrinth().add(null, modrinthVersionEntry.versionId(), modrinthVersionEntry.versionId).metaFolder(metaFolder).build();
                } else {
                    String potentialSlug = Path.of(fileEntry.path).getFileName().toString();
                    Matcher matcher = Pattern.compile("-[0-9]").matcher(potentialSlug);
                    if(matcher.find()) {
                        potentialSlug = potentialSlug.substring(0, matcher.start()); // Cut off dashes followed by number, those are likely version name.
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

        private record ModrinthVersionEntry(String hash, String projectId, String versionId) {}
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

    enum Sideness {
        CLIENT,
        SERVER,
        BOTH
    }
}
