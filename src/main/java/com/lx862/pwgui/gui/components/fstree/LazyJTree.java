package com.lx862.pwgui.gui.components.fstree;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;

public abstract class LazyJTree extends JTree implements TreeWillExpandListener {
    public LazyJTree() {
        addTreeWillExpandListener(this);
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        MutableTreeNode treeNode = (MutableTreeNode) event.getPath().getLastPathComponent();
        if(treeNode instanceof LazyLoadedDefaultTreeNode lazyNode) {
            if(!lazyNode.loaded()) {
                loadLazyNode(lazyNode);
                lazyNode.markLoaded();
            }
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    public abstract void loadLazyNode(LazyLoadedDefaultTreeNode node);
}
