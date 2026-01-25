package com.lx862.pwgui.gui.prompt;

import com.lx862.pwgui.executable.BatchedProgramExecution;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchedExecutionProgressDialog extends ProgressDialog {
    private final BatchedProgramExecution batchedProgramExecution;

    public BatchedExecutionProgressDialog(Window parent, String title, String executionReason, BatchedProgramExecution batchedProgramExecution) {
        super(parent, title);
        this.batchedProgramExecution = batchedProgramExecution;
        int totalCommands = batchedProgramExecution.getTotalCommands();

        AtomicInteger executedCommands = new AtomicInteger();
        batchedProgramExecution.onProgramStart(programExecution -> {
            setStatus(String.format("Running command \"%s\"", programExecution.getCommand()));
            programExecution.onOutput((stdoutContext -> {
                setStatus(stdoutContext.content());
            }));
        });

        batchedProgramExecution.onProgramExit(exitCode -> {
            setProgress((double)executedCommands.incrementAndGet() / totalCommands);
        });

        batchedProgramExecution.onExit(success -> {
            SwingUtilities.invokeLater(this::dispose);
        });
        setStatus(String.format("Execute %d commands...", totalCommands));
        batchedProgramExecution.execute(executionReason);
    }

    @Override
    protected void onCancellation() {
        batchedProgramExecution.terminate();
    }

    @Override
    public void dispose() {
        batchedProgramExecution.terminate();
        super.dispose();
    }
}
