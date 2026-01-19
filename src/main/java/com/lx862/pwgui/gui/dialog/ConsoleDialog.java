package com.lx862.pwgui.gui.dialog;

import com.lx862.pwgui.core.Constants;
import com.lx862.pwgui.executable.Executable;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ConsoleDialog extends BaseDialog {
    private final List<String> commandHistory;
    private final Executable executable;
    private final KTextArea logTextArea;
    private int commandHistoryIndex;
    private ProgramExecution currentExecution;

    public ConsoleDialog(Executable executable, Window parent) {
        super(parent, Util.withTitlePrefix(String.format("%s Console", executable.getProgramName())));

        setSize(600, 450);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.commandHistory = new ArrayList<>();
        this.executable = executable;

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JLabel descriptionLabel = new JLabel(String.format("Here you can directly run %s commands.", executable.getProgramName()));
        contentPanel.add(descriptionLabel, BorderLayout.NORTH);

        logTextArea = new KTextArea();
        logTextArea.useMonospacedFont();
        logTextArea.setEditable(false);
        contentPanel.add(new JScrollPane(logTextArea));

        KGridBagLayoutPanel actionPanel = new KGridBagLayoutPanel(4, 0, 3);
        actionPanel.setBorder(GUIHelper.getPaddedBorder(6, 0, 0, 0));

        KTextField commandInputField = new KTextField("help");
        commandInputField.addActionListener(new RunCommandAction(commandInputField));

        commandInputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                boolean arrowKeyPressed = keyEvent.getKeyCode() == KeyEvent.VK_UP || keyEvent.getKeyCode() == KeyEvent.VK_DOWN;
                if(arrowKeyPressed) {
                    String command = getCommandHistory(keyEvent.getKeyCode() == KeyEvent.VK_UP);
                    if(command == null) return;
                    commandInputField.setText(command);
                }
            }
            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });

        KButton runCommandButton = new KButton(new RunCommandAction(commandInputField));
        actionPanel.addRow(1, 1, new JLabel(executable.getProgramName()), commandInputField, runCommandButton);
        contentPanel.add(actionPanel, BorderLayout.PAGE_END);
        executeCommand("", true);

        add(contentPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(commandInputField::requestFocus); // Focus on command input right away
    }

    private String getCommandHistory(boolean prev) {
        if(commandHistory.isEmpty()) return null;

        commandHistoryIndex = commandHistoryIndex - (prev ? 1 : -1);
        if(commandHistoryIndex < 0) commandHistoryIndex = 0;
        if(commandHistoryIndex >= commandHistory.size()) commandHistoryIndex = commandHistory.size() - 1;
        return commandHistory.get(commandHistoryIndex);
    }

    private void executeCommand(String args, boolean helpMessage) {
        commandHistory.add(args);
        commandHistoryIndex = commandHistory.size();

        if(currentExecution != null) {
            logTextArea.append(String.format("\n> %s\n", args));
            currentExecution.enterInput(args);
        } else {
            if(!helpMessage) {
                logTextArea.append(String.format("\n> %s %s\n", executable.getProgramName(), args));
                commandHistory.add(args);
            }

            StringTokenizer argsTokenizer = new StringTokenizer(args);
            String[] splitArgs = new String[argsTokenizer.countTokens()];

            for(int i = 0; argsTokenizer.hasMoreTokens(); ++i) {
                splitArgs[i] = argsTokenizer.nextToken();
            }

            ProgramExecution programExecution = Executables.packwiz.buildCommand(splitArgs).build();
            currentExecution = programExecution;
            programExecution.onOutput(line -> {
                logTextArea.append(line.content() + "\n");
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            });
            programExecution.onExit(exitCode -> {
                if(helpMessage) {
                    logTextArea.select(0, 0);
                } else {
                    logTextArea.append(String.format("Exited with code %d\n", exitCode));
                }
                currentExecution = null;
            });
            programExecution.run(helpMessage ? "Display help message" : Constants.REASON_TRIGGERED_BY_USER);
        }
    }

    class RunCommandAction extends AbstractAction {
        private final JTextField commandInputField;
        public RunCommandAction(JTextField commandInputField) {
            super("Run");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            this.commandInputField = commandInputField;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            executeCommand(commandInputField.getText(), false);
            commandInputField.setText("");
        }
    }
}
