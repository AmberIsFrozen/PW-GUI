package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.core.data.model.file.FileSystemEntityModel;

import javax.swing.tree.MutableTreeNode;
import java.nio.file.Path;

public class FileSystemSortedTreeNode extends LazyLoadedDefaultTreeNode implements Comparable<MutableTreeNode> {
    public final String name;
    public final Path path;
    private final int initialChildFileCount;

    public FileSystemSortedTreeNode(FileSystemEntityModel model) {
        this.path = model.path;
        this.name = model.name;

        String[] fileList = this.path.toFile().list();
        this.initialChildFileCount = fileList == null ? 0 : fileList.length;
        setUserObject(model);
    }

    @Override
    public String toString() {
        return String.format("FileSystemSortedTreeNode[name=%s, path=%s]", name, path);
    }

    public void addAndSort(MutableTreeNode newChild) {
        super.add(newChild);
        sort();
    }

    public boolean containsNode(MutableTreeNode node) {
        return children != null && children.contains(node);
    }

    public void sort() {
        if(this.children != null) {
            this.children.sort(null);
        }
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(!(other instanceof FileSystemSortedTreeNode)) return false;

        return path.equals(((FileSystemSortedTreeNode)other).path);
    }

    @Override
    public int compareTo(MutableTreeNode otherNode) {
        if(otherNode instanceof FileSystemSortedTreeNode other) {
            int bl1 = Boolean.compare(other.path.toFile().isDirectory(), path.toFile().isDirectory());
            if(bl1 == 0) {
                return name.compareToIgnoreCase(other.name);
            } else {
                return bl1;
            }
        }
        return 1;
    }

    @Override
    public boolean guessIfNodeIsLeaf() {
        return initialChildFileCount == 0;
    }
}
