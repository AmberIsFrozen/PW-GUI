package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.core.data.model.file.FileSystemEntityModel;
import com.lx862.pwgui.gui.components.kui.KTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class FileSystemTreeCellRenderer extends KTreeCellRenderer {
    private static final int IGNORED_COLOR_ALPHA = 75;

    private final Color newFileColor;
    private boolean fileIsNew = false;

    public FileSystemTreeCellRenderer(Color newFileColor) {
        this.newFileColor = newFileColor;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean selected, boolean expanded, boolean leaf, int row, boolean b3) {
        super.getTreeCellRendererComponent(jTree, o, selected, expanded, leaf, row, b3);
        if(jTree instanceof FileSystemTree fileSystemTree) {
            Font defaultFont = UIManager.getFont("defaultFont");
            if(o instanceof FileSystemSortedTreeNode fileSystemSortedTreeNode) {
                FileSystemEntityModel fileInfo = (FileSystemEntityModel)fileSystemSortedTreeNode.getUserObject();

                if(fileSystemTree.shouldIgnore(fileInfo.path)) {
                    Color lowOpacityColor = new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), IGNORED_COLOR_ALPHA);
                    setForeground(lowOpacityColor);
                }

                setText(fileInfo.getDisplayName());
                setFont(defaultFont.deriveFont(fileInfo.isUserFriendlyName() ? Font.ITALIC : Font.PLAIN));
                setIcon(fileInfo.getIcon());

                fileIsNew = fileSystemTree.isNewFile(fileSystemSortedTreeNode.path);
            } else {
                setFont(defaultFont.deriveFont(Font.PLAIN));
                setText(o.toString());
            }
        } else {
            throw new IllegalStateException("FileSystemTreeCellRenderer not attached to a FileSystemTree!");
        }

        return this;
    }

    @Override
    public void paint(Graphics g) {
        if(fileIsNew) {
            g.setColor(newFileColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        super.paint(g);
    }
}
