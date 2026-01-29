package com.lx862.pwgui.gui.panel.editing;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.*;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.gui.components.kui.KSplitPane;
import com.lx862.pwgui.gui.panel.editing.filetype.*;
import com.lx862.pwgui.gui.NameTabPair;
import com.lx862.pwgui.gui.components.fstree.FileSystemSortedTreeNode;
import com.lx862.pwgui.gui.panel.editing.filetype.content.AddContentPanel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EditPanel extends JPanel {
    private final FileBrowserPanel fileBrowserPanel;
    private final FileDetailPanel fileDetailPanel;

    public EditPanel(Modpack modpack) {
        setLayout(new BorderLayout());

        this.fileDetailPanel = new FileDetailPanel();
        this.fileBrowserPanel = new FileBrowserPanel(modpack);

        fileBrowserPanel.fileBrowserTree.addTreeSelectionListener(treeSelectionEvent -> {
            if(!fileBrowserPanel.fileBrowserTree.fsLock) { // File browser tree also triggers the same event if our selected file is modified externally. In this case, we should just let them override our changes, as it's probably external changes.
                saveChanges(true);
            }

            fileBrowserPanel.updateIgnorePattern(null);

            List<FileSystemSortedTreeNode> selectedNodes = new ArrayList<>();
            if(fileBrowserPanel.fileBrowserTree.getSelectionPaths() != null) {
                for(TreePath path : fileBrowserPanel.fileBrowserTree.getSelectionPaths()) {
                    FileSystemSortedTreeNode node = (FileSystemSortedTreeNode) path.getLastPathComponent();
                    selectedNodes.add(node);
                }
            }

            List<NameTabPair> filePanels;
            if(selectedNodes.isEmpty()) {
                filePanels = Collections.emptyList();
            } else {
                List<FileSystemEntityModel> entries = selectedNodes.stream().map(e -> (FileSystemEntityModel) e.getUserObject()).toList();
                filePanels = getViews(new FileEntryPaneContext(modpack, fileBrowserPanel::updateIgnorePattern, fileDetailPanel.saveButton::setEnabled, () -> saveChanges(true)), entries);
                Collections.reverse(filePanels);
            }
            fileDetailPanel.setTabs(filePanels);
        });

        KSplitPane splitPane = new KSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileBrowserPanel, fileDetailPanel, 0.5);
        add(splitPane);
    }

    public void onFileChange(WatchEvent.Kind<?> kind, Path path) {
        fileBrowserPanel.fileBrowserTree.onFileChange(kind, path);
    }

    public void saveChanges(boolean notify) {
        fileDetailPanel.saveAllTabs(notify);
    }

    private static List<NameTabPair> getViews(FileEntryPaneContext context, List<FileSystemEntityModel> nodes) {
        List<NameTabPair> panels = new ArrayList<>();
        try {
            if(nodes.size() > 1) {
                addPanel(panels, () -> new NameTabPair("Multiple items", new MultipleItemsPanel(context, nodes)));
            } else {
                FileSystemEntityModel node = nodes.get(0);
                if(node instanceof DirectoryModel) addPanel(panels, () -> new NameTabPair("Folder", new DirectoryPanel(context, (DirectoryModel)node)));
                if(node instanceof GenericFileModel) addPanel(panels, () -> new NameTabPair("File", new FilePanel(context, (GenericFileModel)node)));
                if(node instanceof ContentDirectoryModel) addPanel(panels, () -> new NameTabPair(String.format("Add new %s", node.getDisplayName()), new AddContentPanel(context, (ContentDirectoryModel) node)));
                if(node instanceof PlainTextFileModel) addPanel(panels, () -> new NameTabPair("Plain Text", new PlainTextPanel(context, (PlainTextFileModel) node)));
                if(node instanceof ConfigDirectoryModel) addPanel(panels, () -> new NameTabPair("Config Folder", new ConfigDirectoryPanel(context, (DirectoryModel)node)));
                if(node instanceof GitIgnoreFileModel) addPanel(panels, () -> new NameTabPair("Git Ignore", new GitIgnorePanel(context, (GitIgnoreFileModel) node)));
                if(node instanceof ImageFileModel) addPanel(panels, () -> new NameTabPair("Image Preview", new ImagePanel(context, (ImageFileModel) node)));
                if(node instanceof LicenseFileModel) addPanel(panels, () -> new NameTabPair("License", new LicenseFilePanel(context, (LicenseFileModel) node)));
                if(node instanceof MarkdownFileModel) addPanel(panels, () -> new NameTabPair("Markdown", new MarkdownPanel(context, (MarkdownFileModel) node)));
                if(node instanceof MinecraftOptionsFileModel) addPanel(panels, () -> new NameTabPair("Minecraft Options", new MinecraftOptionPanel(context, (MinecraftOptionsFileModel) node)));
                if(node instanceof ModpackConfigFileModel) addPanel(panels, () -> {
                    try {
                        return new NameTabPair("Modpack Config", new ModpackConfigPanel(context, (ModpackConfigFileModel) node));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                if(node instanceof ModpackIndexFileModel) addPanel(panels, () -> new NameTabPair("Packwiz Index", new ModpackIndexFilePanel(context, (ModpackIndexFileModel) node)));
                if(node instanceof PackwizIgnoreFileModel) addPanel(panels, () -> new NameTabPair("Packwiz Ignore", new PackwizIgnorePanel(context, (PackwizIgnoreFileModel) node)));
                if(node instanceof PackMetadataFileModel) addPanel(panels, () -> new NameTabPair("Packwiz Meta File", new PackwizMetaPanel(context, (PackMetadataFileModel) node)));
            }
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
        }

        return panels;
    }

    private static void addPanel(List<NameTabPair> panels, Supplier<NameTabPair> getPanel) {
        try {
            panels.add(getPanel.get());
        } catch (Exception e) {
            panels.add(new NameTabPair("Error", new ErrorPanel(e)));
            PWGUI.LOGGER.error("Failed to initialize panel!");
            PWGUI.LOGGER.error("", e);
        }
    }
}

