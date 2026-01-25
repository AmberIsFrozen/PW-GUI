package com.lx862.pwgui.task;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public abstract class Task {
    private final List<Consumer<OutputMessage>> outputListeners;
    private final List<Consumer<ExitResult>> exitListeners;
    private final List<Consumer<Float>> progressListeners;
    private final String taskName;
    protected final ExecutorService defaultExecutor;

    public Task(String taskName, ExecutorService defaultExecutor) {
        this.taskName = taskName;
        this.defaultExecutor = defaultExecutor;
        this.outputListeners = new ArrayList<>();
        this.exitListeners = new ArrayList<>();
        this.progressListeners = new ArrayList<>();
    }

    public Task onOutput(Consumer<OutputMessage> consumer) {
        this.outputListeners.add(consumer);
        return this;
    }

    public Task onExit(Consumer<ExitResult> consumer) {
        this.exitListeners.add(consumer);
        return this;
    }

    public Task onProgress(Consumer<Float> consumer) {
        this.progressListeners.add(consumer);
        return this;
    }

    public final void run(String reason) {
        run(reason, this.defaultExecutor);
    }

    public abstract void run(String reason, ExecutorService executor);

    public abstract void terminate();

    protected void submitOutput(String message) {
        submitOutput(new OutputMessage(message, false));
    }

    protected void submitOutput(OutputMessage outputMessage) {
        invokeCallback(outputListeners, outputMessage);
    }

    protected void submitProgress(float value) {
        invokeCallback(progressListeners, value);
    }

    protected void submitExitResult(ExitResult exitResult) {
        invokeCallback(exitListeners, exitResult);
    }

    protected <T> void invokeCallback(List<Consumer<T>> callbacks, T value) {
        for(Consumer<T> callback : callbacks) {
            SwingUtilities.invokeLater(() -> callback.accept(value));
        }
    }

    public String getTaskName() {
        return this.taskName;
    }

    public record OutputMessage(String content, boolean isPrompt) {
    }

    public record ExitResult(boolean success, int exitCode, Exception exception) {
        public static ExitResult ok() {
            return code(0);
        }
        public static ExitResult code(int exitCode) {
            return new ExitResult(exitCode == 0, exitCode, null);
        }
        public static ExitResult exception(int exitCode, Exception ex) {
            return new ExitResult(false, exitCode, ex);
        }
    }
}
