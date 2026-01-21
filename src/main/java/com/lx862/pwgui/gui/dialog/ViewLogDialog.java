package com.lx862.pwgui.gui.dialog;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.BuildMetadata;
import com.lx862.pwgui.core.Logger;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.prompt.FileSavedDialog;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Dialog to view the program's log */
public class ViewLogDialog extends BaseDialog {
    private final Logger.LogCallback appendLogCallback;
    private final StringBuilder logHistory;

    public ViewLogDialog(JFrame frame) {
        super(frame, Util.withTitlePrefix("View Program Log"));

        setSize(600, 400);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.logHistory = new StringBuilder();

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JLabel descriptionLabel = new JLabel(String.format("This displays the program log for %s, which may be useful for diagnosing issues", BuildMetadata.INSTANCE.name));
        contentPanel.add(descriptionLabel, BorderLayout.NORTH);

        JTextPane logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        JScrollPane logTextAreaScrollPane = new JScrollPane(logTextPane);
        contentPanel.add(logTextAreaScrollPane, BorderLayout.CENTER);

        Style style = logTextPane.addStyle("Log Style", null);
        this.appendLogCallback = (line, realtime) -> {
            Color logColor = line.contains("[WARN]") ? new Color(0xFF8800) : line.contains("[ERROR]") ? Color.RED : Color.BLACK;
            StyleConstants.setForeground(style, logColor);

            Document doc = logTextPane.getDocument();
            try {
                doc.insertString(doc.getLength(), line + "\n\n", style);
                logHistory.append(line).append("\n");
            } catch (BadLocationException ignored) {}
            logTextAreaScrollPane.getVerticalScrollBar().setValue(logTextAreaScrollPane.getVerticalScrollBar().getMaximum()); // Jump to bottom
            SimpleAttributeSet aSet = new SimpleAttributeSet();
        };

        PWGUI.LOGGER.addListener(appendLogCallback);

        KButton saveAsButton = new KButton(new SaveLogAction());
        KActionPanel actionPanel = new KActionPanel.Builder().add(saveAsButton).build();

        contentPanel.add(actionPanel, BorderLayout.PAGE_END);
        add(contentPanel);
    }

    @Override
    public void dispose() {
        super.dispose();
        PWGUI.LOGGER.removeListener(appendLogCallback);
    }

    class SaveLogAction extends AbstractAction {
        public SaveLogAction() {
            super("Save As...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            KFileChooser fileChooser = new KFileChooser("save-log");
            if (fileChooser.openSaveAsDialog(ViewLogDialog.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    FileUtils.writeStringToFile(file, logHistory.toString(), StandardCharsets.UTF_8);
                    new FileSavedDialog(ViewLogDialog.this, "Log Saved!", file).setVisible(true);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ViewLogDialog.this, String.format("Failed to save log:\n%s", e.getMessage()), Util.withTitlePrefix("Save Log"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
