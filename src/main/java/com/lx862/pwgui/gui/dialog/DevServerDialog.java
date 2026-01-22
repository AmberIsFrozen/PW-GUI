package com.lx862.pwgui.gui.dialog;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.components.kui.KTextArea;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.executable.ProgramExecution;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DevServerDialog extends BaseDialog {
    private final KTextArea logTextArea;
    private final KButton startButton;
    private final KButton stopButton;
    private ExecutorService serverExecutor;
    private ProgramExecution packwizServeProgram;

    public DevServerDialog(JFrame frame) {
        super(frame, Util.withTitlePrefix("Packwiz Serve"));

        setSize(400, 400);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JLabel description = new JLabel("<html>This runs a local HTTP server for development, allowing packwiz-installer to reference a local pack.</html>");
        contentPanel.add(description, BorderLayout.NORTH);

        logTextArea = new KTextArea();
        logTextArea.useMonospacedFont();
        logTextArea.setEditable(false);
        contentPanel.add(new JScrollPane(logTextArea), BorderLayout.CENTER);

        StartServerAction startServerAction = new StartServerAction();

        startButton = new KButton(startServerAction);
        stopButton = new KButton(new StopServerAction());

        startServerAction.startServer();
        startButton.setVisible(false);

        KActionPanel actionPanel = new KActionPanel.Builder().setNegativeButton(stopButton).setPositiveButton(startButton).build();
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    class StartServerAction extends AbstractAction {

        public StartServerAction() {
            super("Start");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            stopButton.setVisible(true);
            startButton.setVisible(false);
            startServer();
        }

        private void startServer() {
            serverExecutor = Executors.newSingleThreadExecutor();
            logTextArea.append("----- Development Server Started -----\n");
            packwizServeProgram = PackwizExecutable.INSTANCE.serve().build();
            packwizServeProgram.onOutput((line) -> logTextArea.append(line.content() + "\n"));

            try {
                packwizServeProgram.run("Launched by user", serverExecutor);
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                logTextArea.append(Util.withBracketPrefix(String.format("ERROR: %s", e.getMessage())));
            }
        }
    }

    class StopServerAction extends AbstractAction {
        public StopServerAction() {
            super("Stop");
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            stopServer();
            stopButton.setVisible(false);
            startButton.setVisible(true);
        }

        private void stopServer() {
            if(packwizServeProgram != null) packwizServeProgram.terminate();
            serverExecutor.shutdown();
            logTextArea.append("----- Development Server Stopped -----\n");
        }
    }

    @Override
    public void dispose() {
        new StopServerAction().stopServer();
        super.dispose();
    }
}
