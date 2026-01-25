package com.lx862.pwgui.gui.prompt;

import com.lx862.pwgui.executable.Task;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TaskDialog extends ProgressDialog {
    private final Task task;
    private final AtomicReference<String> lastOutput = new AtomicReference<>(); // TODO: Keep the whole log for more context
    private Supplier<Boolean> taskErroredSupplier;

    public TaskDialog(Window window, String title, Task task) {
        super(window, title);
        this.task = task;

        task.onOutput(stdout -> {
            lastOutput.set(stdout.content());
            setStatus(stdout.content());
        });

        task.onExit(exitResult -> {
            if(!exitResult.success()) {
                showErrorDialog(exitResult);
            }
            dispose();
        });
        task.onProgress(this::setProgress);

        setStatus(String.format("Waiting for %s...", task.getTaskName()));
    }

    protected void showErrorDialog(Task.ExitResult exitResult) {
        if(!exitResult.success() && exitResult.exitCode() != -1) { // -1 reserved for termination exit.
            if(taskErroredSupplier != null && !taskErroredSupplier.get()) return; // it's not considered an error
            String formattedMessage = String.format("%s exited with code %d:\n%s", task.getTaskName(), exitResult.exitCode(), lastOutput.get());
            JOptionPane.showMessageDialog(this, formattedMessage, Util.withTitlePrefix(task.getTaskName()), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void whenProgramErrored(Supplier<Boolean> supplier) {
        this.taskErroredSupplier = supplier;
    }

    @Override
    protected void onCancellation() {
        task.terminate();
    }

    @Override
    public void dispose() {
        task.terminate();
        super.dispose();
    }
}
