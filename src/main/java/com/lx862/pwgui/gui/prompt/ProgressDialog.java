package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KCollapsibleToggle;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;

public abstract class ProgressDialog extends BaseDialog {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final JTextArea logTextArea;

    public ProgressDialog(Window window, String title) {
        super(window, Util.withTitlePrefix(title), ModalityType.DOCUMENT_MODAL);

        setSize(450, 160);
        setLocationRelativeTo(window);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(progressBar);

        contentPanel.add(GUIConfiguration.createVerticalPadding(10));

        statusLabel = new JLabel("Status text");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(statusLabel);

        contentPanel.add(GUIConfiguration.createVerticalPadding(10));

        logTextArea = new JTextArea(10, 0);
        logTextArea.setEditable(false);
        logTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actionRow = new JPanel();
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.LINE_AXIS));

        KCollapsibleToggle logToggle = new KCollapsibleToggle("Show Log", "Hide Log");
        logToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.add(logToggle);

        actionRow.add(Box.createHorizontalGlue());

        KButton cancelButton = new KButton("Cancel");
        cancelButton.addActionListener((action) -> {
            dispose();
        });
        actionRow.add(cancelButton);

        JScrollPane logWrapper = new JScrollPane(logTextArea);
        logWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        logWrapper.setVisible(false);

        logToggle.addItemListener(itemEvent -> {
            logWrapper.setVisible(logToggle.isSelected());
            setSize(getSize().width, getPreferredSize().height);
        });

        contentPanel.add(actionRow);
        contentPanel.add(logWrapper);
        add(contentPanel);
    }

    /**
     * Set the progress bar of the current progress dialog.
     * @param progress An double value from 0 to 1. Automatically multiplied by 100 and rounded to the nearest integer percentage
     */
    protected void setProgress(double progress) {
        setProgress((int)Math.round(progress * 100));
    }

    /**
     * Set the progress bar of the current progress dialog.
     * @param progress An integer value from 0 to 100
     */
    protected void setProgress(int progress) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(progress);
    }

    protected abstract void onCancellation();

    @Override
    public void dispose() {
        onCancellation();
        super.dispose();
    }

    protected void setStatus(String text) {
        statusLabel.setText(text);
        logTextArea.append(text + "\n");
    }
}
