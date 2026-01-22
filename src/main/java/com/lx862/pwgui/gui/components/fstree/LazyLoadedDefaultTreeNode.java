package com.lx862.pwgui.gui.components.fstree;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class LazyLoadedDefaultTreeNode extends DefaultMutableTreeNode {
    protected boolean contentLoaded = false;

    public void markLoaded() {
        this.contentLoaded = true;
    }

    public boolean loaded() {
        return contentLoaded;
    }

    @Override
    public boolean isLeaf() {
        return getChildCount() == 0 ? guessIfNodeIsLeaf() : super.isLeaf();
    }

    public abstract boolean guessIfNodeIsLeaf();
}
