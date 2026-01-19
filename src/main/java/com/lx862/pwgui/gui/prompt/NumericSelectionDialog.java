package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.gui.action.OKAction;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KGridBagLayoutPanel;
import com.lx862.pwgui.gui.components.kui.KListCellRenderer;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class NumericSelectionDialog extends BaseDialog {
    public <T> NumericSelectionDialog(Window parent, String title, List<T> list, Consumer<Integer> callback) {
        super(parent, Util.withTitlePrefix(title), ModalityType.DOCUMENT_MODAL);
        init(parent, title, list, callback);
    }

    private <T> void init(Component component, String title, List<T> list, Consumer<Integer> callback) {
        KGridBagLayoutPanel contentPanel = new KGridBagLayoutPanel(3, 1);
        contentPanel.setBorder(GUIHelper.getPaddedBorder(10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.addRow(1, titleLabel);
        contentPanel.addRow(1, new JLabel("<html>Multiple projects were found based on your search term<br>Please select which one you'd like to choose</html>"));

        DefaultListModel<T> defaultListModel = new DefaultListModel<>();
        defaultListModel.addAll(list);

        JList<T> jList = new JList<>(defaultListModel);
        jList.setSize(Integer.MAX_VALUE, getHeight());
        jList.setCellRenderer(new KListCellRenderer());
        jList.setSelectedIndex(0);
        jList.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.addRow(1, jList);

        KButton okButton = new KButton(new OKAction(() -> {
            callback.accept(jList.getSelectedIndex());
            dispose();
        }));

        KButton cancelButton = new KButton("Cancel");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(actionEvent -> {
            callback.accept(-1);
            dispose();
        });

        JPanel actionPanel = new KActionPanel.Builder().setPositiveButton(okButton).setNegativeButton(cancelButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.addRow(1, actionPanel);
        add(contentPanel);

        pack();
        setLocationRelativeTo(component);
    }
}
