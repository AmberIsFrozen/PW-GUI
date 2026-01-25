package com.lx862.pwgui.gui.dialog;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.core.log.LogEntry;
import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.gui.components.JListPopupMenu;
import com.lx862.pwgui.gui.components.LogEntryListCellRenderer;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.prompt.FileSavedDialog;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Dialog to view the program's log */
public class ViewLogDialog extends BaseDialog {
    private final Logger.LogCallback appendLogCallback;

    public ViewLogDialog(JFrame frame) {
        super(frame, Util.withTitlePrefix("Program Log"));

        setSize(600, 400);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JLabel descriptionLabel = new JLabel(String.format("This displays the program log for %s, which may be useful for diagnosing issues", ApplicationInfo.INSTANCE.name));
        contentPanel.add(descriptionLabel, BorderLayout.NORTH);

        DefaultListModel<LogEntry> logs = new DefaultListModel<>();
        JList<LogEntry> logListPane = new JList<>(logs);
        logListPane.setCellRenderer(new LogEntryListCellRenderer());

        JListPopupMenu popupMenu = new JListPopupMenu(logListPane);
        popupMenu.add(new JMenuItem(new LogEntryListCellRenderer.CopyLogAction(() -> logs.get(logListPane.getSelectedIndex()))));

        logListPane.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(logListPane);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        this.appendLogCallback = (entry, realtime) -> {
            if(entry.logLevel() == LogEntry.LogLevel.DEBUG && !Config.getInstance().debugMode.value()) return;
            logs.addElement(entry);

            SwingUtilities.invokeLater(() -> {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()); // Jump to bottom
            });
        };

        Logger.addListener(appendLogCallback);

        KButton saveAsButton = new KButton(new SaveLogAction());
        KActionPanel actionPanel = new KActionPanel.Builder().add(saveAsButton).build();

        contentPanel.add(actionPanel, BorderLayout.PAGE_END);
        add(contentPanel);
    }

    @Override
    public void dispose() {
        super.dispose();
        Logger.removeListener(appendLogCallback);
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
                    FileUtils.writeStringToFile(file, String.join(System.lineSeparator(), Arrays.stream(PWGUI.LOGGER.getLogHistory()).map(LogEntry::message).toList()), StandardCharsets.UTF_8);
                    new FileSavedDialog(ViewLogDialog.this, "Log Saved!", file).setVisible(true);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ViewLogDialog.this, String.format("Failed to save log:\n%s", e.getMessage()), Util.withTitlePrefix("Save Log"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
