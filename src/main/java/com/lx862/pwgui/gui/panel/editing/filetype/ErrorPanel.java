package com.lx862.pwgui.gui.panel.editing.filetype;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.gui.components.kui.KTextArea;
import com.lx862.pwgui.gui.GUIConfiguration;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorPanel extends JPanel {
    public ErrorPanel(Exception e) {
        setLayout(new BorderLayout());
        setBorder(GUIConfiguration.getPaddedBorder(10));

        JLabel errorLabel = new JLabel("Failed to load panel content!");
        errorLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));

        add(errorLabel, BorderLayout.NORTH);

        KTextArea stackTraceTextArea = new KTextArea();
        stackTraceTextArea.useMonospacedFont();
        stackTraceTextArea.setEditable(false);

        String stackTraceContent;

        try(StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            stackTraceContent = sw.toString();
        } catch (IOException ex) {
            stackTraceContent = "Failed to obtain stack trace: " + ex.getMessage();
        }

        stackTraceTextArea.setText(stackTraceContent, true);
        add(new JScrollPane(stackTraceTextArea), BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("This is a bug! Consider reporting this to the developer.");
        add(footerLabel, BorderLayout.SOUTH);
    }
}
