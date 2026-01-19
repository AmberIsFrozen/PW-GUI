package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.gui.components.ModUpdateListCellRenderer;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KCollapsibleToggle;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class UpdateSummaryDialog extends BaseDialog {
    public UpdateSummaryDialog(Window parent, List<String> newItems, List<String> skippedItems, List<String> unsupportedItems, Consumer<Boolean> callback) {
        super(parent, Util.withTitlePrefix("Update Summary"), ModalityType.DOCUMENT_MODAL);

        setSize(500, 400);
        setLocationRelativeTo(parent);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel("Update Summary");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel(String.format("%d item(s) can be updated:", newItems.size()));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        DefaultListModel<String> updateModListModel = new DefaultListModel<>();
        for(String str : newItems) {
            updateModListModel.addElement(str);
        }

        JList<String> updateItemsList = new JList<>(updateModListModel);
        updateItemsList.setCellRenderer(new ModUpdateListCellRenderer());

        JScrollPane updateItemsListScrollPane = new JScrollPane(updateItemsList);
        updateItemsListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(updateItemsListScrollPane);

        if(!skippedItems.isEmpty()) {
            DefaultListModel<String> skippedItemsListModel = new DefaultListModel<>();
            for(String mod : skippedItems) {
                skippedItemsListModel.addElement(mod);
            }

            JList<String> skippedItemsList = new JList<>(skippedItemsListModel);
            JScrollPane skippedItemsScrollPane = new JScrollPane(skippedItemsList);
            skippedItemsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            skippedItemsScrollPane.setVisible(false);

            KCollapsibleToggle pinnedItemsToggle = new KCollapsibleToggle(String.format("Show pinned items (%d)", skippedItems.size()), "Hide pinned items");
            pinnedItemsToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
            pinnedItemsToggle.addActionListener(actionEvent -> skippedItemsScrollPane.setVisible(!skippedItemsScrollPane.isVisible()));
            contentPanel.add(pinnedItemsToggle);

            contentPanel.add(skippedItemsScrollPane);
        }

        if(!unsupportedItems.isEmpty()) {
            DefaultListModel<String> unsupportedItemsListModel = new DefaultListModel<>();
            for(String mod : unsupportedItems) {
                unsupportedItemsListModel.addElement(mod);
            }

            JList<String> unsupportedItemsList = new JList<>(unsupportedItemsListModel);
            JScrollPane unsupportedItemsScrollPane = new JScrollPane(unsupportedItemsList);
            unsupportedItemsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            unsupportedItemsScrollPane.setVisible(false);

            KCollapsibleToggle unsupportedItemsToggle = new KCollapsibleToggle(String.format("Show unsupported items (%d)", unsupportedItems.size()), "Hide unsupported items");
            unsupportedItemsToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
            unsupportedItemsToggle.addActionListener(actionEvent -> unsupportedItemsScrollPane.setVisible(!unsupportedItemsScrollPane.isVisible()));
            contentPanel.add(unsupportedItemsToggle);

            contentPanel.add(unsupportedItemsScrollPane);
        }

        JLabel updateConfirmLabel = new JLabel("Do you want to update?");
        updateConfirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(updateConfirmLabel);

        KButton yesButton = new KButton(new ConfirmUpdateAction(callback));
        KButton noButton = new KButton(new CancelUpdateAction(callback));

        JPanel actionPanel = new KActionPanel.Builder().setPositiveButton(yesButton).setNegativeButton(noButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(actionPanel);
        add(contentPanel);
    }

    class ConfirmUpdateAction extends AbstractAction {
        private final Consumer<Boolean> callback;

        public ConfirmUpdateAction(Consumer<Boolean> callback) {
            super("Yes");
            putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
            callback.accept(true);
        }
    }

    class CancelUpdateAction extends AbstractAction {
        private final Consumer<Boolean> callback;

        public CancelUpdateAction(Consumer<Boolean> callback) {
            super("No");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
            callback.accept(false);
        }
    }
}

