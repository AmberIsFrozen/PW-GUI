package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.core.data.model.file.*;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class FileSystemTreeCellRenderer extends DefaultTreeCellRenderer {
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
            if(o instanceof FileSystemSortedTreeNode fileSystemSortedTreeNode) {
                FileSystemEntityModel model = (FileSystemEntityModel)fileSystemSortedTreeNode.getUserObject();

                if(fileSystemTree.shouldIgnore(model.path)) {
                    Color lowOpacityColor = new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), IGNORED_COLOR_ALPHA);
                    setForeground(lowOpacityColor);
                }

                setText(model.getDisplayName());
                setFont(jTree.getFont().deriveFont(model.isUserFriendlyName() ? Font.ITALIC : Font.PLAIN));
                setIcon(getIcon(model));

                fileIsNew = fileSystemTree.isNewFile(fileSystemSortedTreeNode.getModel().path);
            } else {
                setFont(jTree.getFont().deriveFont(Font.PLAIN));
                setText(o.toString());
            }
        } else {
            throw new IllegalStateException("FileSystemTreeCellRenderer not attached to a FileSystemTree!");
        }

        return this;
    }

    private Icon getIcon(FileSystemEntityModel model) {
        if(model instanceof GitDirectoryModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/git.png"), 18));
        }
        if(model instanceof GitIgnoreFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/gitignore.png"), 18));
        }
        if(model instanceof MinecraftOptionsFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/components/minecraft.png"), 18));
        }
        if(model instanceof ModpackConfigFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/settings.png"), 18));
        }
        if(model instanceof ModpackIndexFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/packwiz.png"), 18));
        }
        if(model instanceof ModrinthPackFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/services/modrinth.png"), 18));
        }
        if(model instanceof PackwizIgnoreFileModel) {
            return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/packwizignore.png"), 18));
        }
        if(model instanceof ContentDirectoryModel) {
            switch(model.path.getFileName().toString()) {
                case "mods" -> {
                    return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/mods.png"), 16));
                }
                // TODO resourcepacks & plugins
                case "shaderpacks" -> {
                    return new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/mime/shaderpacks.png"), 16));
                }
            }
        }

        // Fallback
        return model.path.toFile().isDirectory() ? UIManager.getIcon("FileView.directoryIcon") : UIManager.getIcon("FileView.fileIcon");
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
