package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.file.FileSystemEntityModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/** A JTree representing a directory view/file browser. */
public class FileSystemTree extends LazyJTree {
    private static final List<String> excludedDirs = Arrays.asList(
            ".git", // Huge amount of files to walk through, not gonna bother
            ".pwgui-tmp" // Our temp directory, should be deleted shortly after. (Currently used for mod probing)
    );

    private final List<Path> fileNeedingUserAcknowledgement;
    private final TreeConfiguration treeConfiguration;
    public boolean fsLock; // A slight hack to signal to others when a file is changed

    public FileSystemTree(Path root, TreeConfiguration treeConfiguration) {
        this.treeConfiguration = treeConfiguration;
        this.fileNeedingUserAcknowledgement = new ArrayList<>();
        setModel(new DefaultTreeModel(generateRecursiveTree(root), false));
        setRootVisible(false);
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new FileTransferHandler());
        addTreeSelectionListener((treeSelectionEvent) -> {
            FileSystemSortedTreeNode treeNode = (FileSystemSortedTreeNode)treeSelectionEvent.getPath().getLastPathComponent();
            newFileAcknowledged(treeNode.path);
        });
        setShowsRootHandles(true);
    }

    public boolean shouldIgnore(Path path) {
        return treeConfiguration.shouldDimAppearance(path);
    }

    public void markAsNewFile(Path path) {
        fileNeedingUserAcknowledgement.add(path);
    }

    public boolean isNewFile(Path path) {
        return fileNeedingUserAcknowledgement.contains(path);
    }

    /**
     * The file entry has been selected by the user
     */
    public void newFileAcknowledged(Path path) {
        this.fileNeedingUserAcknowledgement.remove(path);
    }

    private FileSystemSortedTreeNode generateRecursiveTree(Path root) {
        FileSystemSortedTreeNode rootNode = new FileSystemSortedTreeNode(treeConfiguration.getModel(root.toFile()));
        return addChildToTree(rootNode);
    }

    private FileSystemSortedTreeNode addChildToTree(FileSystemSortedTreeNode rootNode) {
        File[] files = rootNode.path.toFile().listFiles();
        if(files != null) {
            for(File file : files) {
                FileSystemEntityModel child = treeConfiguration.getModel(file);
                if(child == null) continue;

                FileSystemSortedTreeNode node = new FileSystemSortedTreeNode(child);
                if(!rootNode.containsNode(node)) { // Node might already be added externally (e.g. file watching)
                    rootNode.add(node);
                }
            }
            rootNode.sort();
        }

        return rootNode;
    }

    /** This is used to notify that a file has been created/modified/removed for live update purposes.
    * You are expected to bring your own FileWatcher to the table :) */
    public void onFileChange(WatchEvent.Kind<?> kind, Path filePath) {
        if(kind == ENTRY_CREATE && Files.exists(filePath)) {
            addNode(filePath);
        } else if(kind == ENTRY_MODIFY) {
            FileSystemEntityModel newNode = treeConfiguration.getModel(filePath.toFile());
            if(newNode != null) modifyNode(filePath, newNode);
        } else {
            removeNode(filePath);
        }
    }

    private void addNode(Path target) {
        Path parent = target.getParent();
        FileSystemSortedTreeNode newNode = generateRecursiveTree(target);

        iterateTree((node) -> {
            if(node.path.equals(parent)) {
                if(!node.containsNode(newNode)) {
                    node.addAndSort(newNode);
                    int insertedIndex = node.getIndex(newNode);
                    ((DefaultTreeModel)getModel()).nodesWereInserted(node, new int[]{insertedIndex});
                } else {
                    PWGUI.LOGGER.warn("[FileSystemTree] Node {} already exists in tree!", newNode.path);
                }
            }
        });
    }

    private void modifyNode(Path target, FileSystemEntityModel newFileType) {
        iterateTree((node) -> {
            if(node.path.equals(target)) {
                TreePath treePath = new TreePath(node.getPath());
                node.setUserObject(newFileType);

                if(treePath.equals(getSelectionPath())) { // Currently selected, we need to reload
                    TreeSelectionEvent selectionEvent = new TreeSelectionEvent(this, getSelectionPath(), false, null, null);
                    fsLock = true;
                    fireValueChanged(selectionEvent); // Re-trigger selection even to update main UI
                    fsLock = false;
                }
            }
        });
    }

    private void removeNode(Path target) {
        iterateTree((node) -> {
            if(node.path.equals(target)) {
                ((DefaultTreeModel)getModel()).removeNodeFromParent(node);
            }
        });
    }

    private void iterateTree(Consumer<FileSystemSortedTreeNode> callback) {
        iterateTree((FileSystemSortedTreeNode)getModel().getRoot(), callback);
    }

    private void iterateTree(FileSystemSortedTreeNode node, Consumer<FileSystemSortedTreeNode> callback) {
        callback.accept(node); // This should be directory
        int childNodeCount = node.getChildCount();

        for(int i = 0; i < childNodeCount; i++) {
            if(i >= node.getChildCount()) break;
            FileSystemSortedTreeNode childNode = (FileSystemSortedTreeNode) node.getChildAt(i);
            if (childNode.getChildCount() > 0) {
                iterateTree(childNode, callback);
            } else {
                callback.accept(childNode);
            }
        }
    }

    @Override
    public void loadLazyNode(LazyLoadedDefaultTreeNode node) {
        if(node instanceof FileSystemSortedTreeNode fsNode) {
            addChildToTree(fsNode);
        }
    }

    public interface TreeConfiguration {
        boolean shouldDimAppearance(Path path);
        FileSystemEntityModel getModel(File file);
    }
}
