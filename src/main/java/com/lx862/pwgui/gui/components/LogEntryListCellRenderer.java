package com.lx862.pwgui.gui.components;

import com.lx862.pwgui.core.log.LogEntry;
import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class LogEntryListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBorder(GUIConfiguration.getPaddedBorder(3, 6, 3, 6));
        if(value instanceof LogEntry logEntry) {
            setText("<html>" + logEntry.message() + "</html>");

            if(isSelected) {
                setForeground(super.getForeground());
            } else {
                Color logColor = logEntry.logLevel() == LogEntry.LogLevel.WARNING ? new Color(0xFF8800) : logEntry.logLevel() == LogEntry.LogLevel.ERROR ? Color.RED : super.getForeground();
                setForeground(logColor);
            }
        }

        return this;
    }

    public static class CopyLogAction extends AbstractAction {
        private final Supplier<LogEntry> logEntry;

        public CopyLogAction(Supplier<LogEntry> logEntry) {
            super("Copy");
            this.logEntry = logEntry;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(logEntry.get().message()), null);
        }
    }
}
