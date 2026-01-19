package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class FileSavedDialog extends BaseDialog {
    public FileSavedDialog(JDialog parentDialog, String title, File file) {
        super(parentDialog, Util.withTitlePrefix(title), true);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h3.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel("Your file is saved under:");
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        JLabel pathLabel = new JLabel(file.toPath().toString());
        pathLabel.setFont(getFont().deriveFont(Font.BOLD));
        pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(pathLabel);

        KActionPanel.Builder actionPanelBuilder = new KActionPanel.Builder();

        if(file.isFile()) {
            KButton openFileButton = new KButton(new OpenFileAction(file));
            actionPanelBuilder.add(openFileButton);
        }

        KButton openDirectoryButton = new KButton(new OpenDirectoryAction(file));
        KButton copyPathButton = new KButton(new CopyPathAction(file));
        KButton finishButton = new KButton(new CloseDialogAction());

        actionPanelBuilder.add(openDirectoryButton, copyPathButton, finishButton);

        KActionPanel actionPanel = actionPanelBuilder.build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(actionPanel);
        add(contentPanel);
        pack();
        setLocationRelativeTo(parentDialog);
        finishButton.requestFocusInWindow(); // Default to focus on the finish button
    }

    class OpenFileAction extends AbstractAction {
        private final File file;

        public OpenFileAction(File file) {
            super("Open File");
            this.file = file;
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Util.tryOpenFile(file);
            FileSavedDialog.this.dispose();
        }
    }

    class OpenDirectoryAction extends AbstractAction {
        private final File file;

        public OpenDirectoryAction(File file) {
            super("Open Folder");
            this.file = file;
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            File containingDirectory = file.isDirectory() ? file : file.getParentFile();
            Util.tryOpenFile(containingDirectory);
            FileSavedDialog.this.dispose();
        }
    }

    class CopyPathAction extends AbstractAction {
        private final File file;

        public CopyPathAction(File file) {
            super("Copy Path");
            this.file = file;
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Util.copyToClipboard(file.toPath().toString());
            FileSavedDialog.this.dispose();
        }
    }

    class CloseDialogAction extends AbstractAction {
        public CloseDialogAction() {
            super("Finish!");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            FileSavedDialog.this.dispose();
        }
    }
}
