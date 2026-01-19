package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.pwcore.PackwizMetaFile;
import com.lx862.pwgui.gui.components.ModDetailListCellRenderer;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class IncompatibleSummaryDialog extends BaseDialog {
    public IncompatibleSummaryDialog(Window parent, List<PackwizMetaFile> items) {
        super(parent, Util.withTitlePrefix("Compatibility Summary"), ModalityType.DOCUMENT_MODAL);

        setSize(500, 400);
        setLocationRelativeTo(parent);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel("Compatibility Summary");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel(String.format("%d item(s) does not have a compatible version for the current modpack:", items.size()));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        DefaultListModel<PackwizMetaFile> updateModListModel = new DefaultListModel<>();
        updateModListModel.addAll(items);

        JList<PackwizMetaFile> updateItemsList = new JList<>(updateModListModel);
        updateItemsList.setCellRenderer(new ModDetailListCellRenderer());

        JScrollPane updateItemsListScrollPane = new JScrollPane(updateItemsList);
        updateItemsListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(updateItemsListScrollPane);

        JLabel updateConfirmLabel = new JLabel("What would you like to do?");
        updateConfirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(updateConfirmLabel);

        KButton copyButton = new KButton(new CopyIncompatibleMetaAction(items));
        KButton removeButton = new KButton(new RemoveItemAction(items));
        KButton ignoreButton = new KButton(new IgnoreAction());

        JPanel actionPanel = new KActionPanel.Builder().add(copyButton, removeButton, ignoreButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(actionPanel);
        add(contentPanel);
    }

    class IgnoreAction extends AbstractAction {
        public IgnoreAction() {
            super("Ignore");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
        }
    }

    class RemoveItemAction extends AbstractAction {
        private final List<PackwizMetaFile> metas;

        public RemoveItemAction(List<PackwizMetaFile> metas) {
            super("Remove Items");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            this.metas = metas;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int successfulRemoval = 0;
            for(PackwizMetaFile meta : metas) {
                try {
                    Files.delete(meta.getPath());
                    successfulRemoval++;
                } catch (IOException e) {
                    PWGUI.LOGGER.exception(e);
                }
            }
            if(successfulRemoval == metas.size()) {
                JOptionPane.showMessageDialog(IncompatibleSummaryDialog.this, String.format("Removed %d incompatible item(s)", successfulRemoval), "Removed incompatible items", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(IncompatibleSummaryDialog.this, String.format("Removed %d incompatible item(s).\nFailed to remove %d item(s), see log for details!", successfulRemoval, metas.size() - successfulRemoval), "Removed incompatible items", JOptionPane.WARNING_MESSAGE);
            }
            dispose();
        }
    }

    class CopyIncompatibleMetaAction extends AbstractAction {
        private final List<PackwizMetaFile> metas;

        public CopyIncompatibleMetaAction(List<PackwizMetaFile> metas) {
            super("Copy to clipboard");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            this.metas = metas;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            StringBuilder sb = new StringBuilder();
            for(PackwizMetaFile meta : metas) {
                sb.append("Name: ").append(meta.name).append("\n");
                sb.append("File name: ").append(meta.fileName).append("\n");
                sb.append("Project Link: ").append(meta.getProjectPageURL()).append("\n");
                sb.append("\n");
            }

            Util.copyToClipboard(sb.toString());
            dispose();
        }
    }
}

