package com.lx862.pwgui.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Executes multiple tasks and invokes callback after completion of all commands */
public class BatchedTask extends Task {
    private final List<Task> tasks;
    private boolean startedExecution = false;

    public BatchedTask(String taskName) {
        this(taskName, Executors.newSingleThreadExecutor(), true);
    }

    public BatchedTask(String taskName, ExecutorService executorService) {
        this(taskName, executorService, false);
    }

    private BatchedTask(String taskName, ExecutorService executorService, boolean ownExecutor) {
        super(taskName, executorService);
        this.tasks = new ArrayList<>();

        if(ownExecutor) {
            onExit(exitResult -> {
                executorService.shutdownNow();
            });
        }
    }

    /** Add another program to queued for execution */
    public void add(RunProgramTask exec) {
        if(startedExecution) throw new IllegalStateException("No more task should be added after batched task has been started!");
        tasks.add(exec);
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        startedExecution = true;
        if(tasks.isEmpty()) { // Nothing to run
            submitExitResult(ExitResult.ok());
            return;
        }

        AtomicInteger erroredCommands = new AtomicInteger();
        AtomicInteger executedCommands = new AtomicInteger();
        int totalCommands = tasks.size();

        submitOutput(new OutputMessage(String.format("Executing %d commands...", totalCommands), false));

        for(Task programExecution : tasks) {
            programExecution.onOutput(this::submitOutput);

            programExecution.onExit(exitCode -> {
                if(!exitCode.success()) erroredCommands.incrementAndGet();
                executedCommands.incrementAndGet();

                submitProgress((float) executedCommands.get() / totalCommands);

                boolean allCommandExecuted = executedCommands.get() == totalCommands;
                if(allCommandExecuted) {
                    submitExitResult(erroredCommands.get() == 0 ? ExitResult.ok() : ExitResult.code(erroredCommands.get()));
                }
            });
            programExecution.run(reason, executor);
        }
    }

    /**
     * Terminate process and shutdown executors
     */
    @Override
    public void terminate() {
        tasks.forEach(Task::terminate);
    }
}
