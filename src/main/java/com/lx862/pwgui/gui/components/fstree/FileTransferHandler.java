package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.PWGUI;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileTransferHandler extends TransferHandler {
    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop() || !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }

        FileSystemTree tree = (FileSystemTree)info.getComponent();
        JTree.DropLocation dropLocation = (JTree.DropLocation)info.getDropLocation();
        TreePath nodePath = dropLocation.getPath();
        if(nodePath.getLastPathComponent() instanceof FileSystemSortedTreeNode node) {
            Path pasteDirectory = node.path.toFile().isDirectory() ? node.path : node.path.getParent();
            Transferable transferable = info.getTransferable();

            try {
                for(File file : (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
                    Path destinationPath = pasteDirectory.resolve(file.getName());
                    if(Files.exists(destinationPath)) {
                        int shouldOverwrite = JOptionPane.showConfirmDialog(tree.getTopLevelAncestor(), String.format("File/folder \"%s\" already exists, do you want to overwrite the file?", file.getName()), "Overwrite file?", JOptionPane.YES_NO_OPTION);
                        if(shouldOverwrite == JOptionPane.NO_OPTION) {
                            return false;
                        }
                    }

                    if(file.isDirectory()) {
                        FileUtils.copyDirectory(file, destinationPath.toFile());
                    } else {
                        Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    tree.markAsNewFile(destinationPath);
                }
            } catch (UnsupportedFlavorException ignored) {
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(tree.getTopLevelAncestor(), "An error occured while copying file/folder:\n" + e, "Failed to Copy!", JOptionPane.ERROR_MESSAGE);
            }

            tree.expandPath(nodePath);
        }

        return true;
    }
}
