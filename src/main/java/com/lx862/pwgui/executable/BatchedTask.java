package com.lx862.pwgui.executable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Executes multiple tasks and invokes callback after completion of all commands */
public class BatchedTask extends Task {
    private final List<Task> programExecutions;
    private final List<Consumer<ExitResult>> programExitCallbacks;
    private boolean startedExecution = false;

    public BatchedTask(String taskName) {
        this(taskName, Executors.newSingleThreadExecutor(), true);
    }

    public BatchedTask(String taskName, ExecutorService executorService) {
        this(taskName, executorService, false);
    }

    private BatchedTask(String taskName, ExecutorService executorService, boolean ownedExecutor) {
        super(taskName, executorService);
        this.programExecutions = new ArrayList<>();
        this.programExitCallbacks = new ArrayList<>();

        if(ownedExecutor) {
            onExit(exitCode -> {
                executorService.shutdownNow();
            });
        }
    }

    /** Add another program to queued for execution */
    public void add(ProgramExecution exec) {
        if(startedExecution) throw new IllegalStateException("No more task should be added after batched task has been started!");
        programExecutions.add(exec);
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        startedExecution = true;
        if(programExecutions.isEmpty()) { // Nothing to run
            callExitListeners(ExitResult.ok());
            return;
        }

        AtomicInteger erroredCommands = new AtomicInteger();
        AtomicInteger executedCommands = new AtomicInteger();
        int totalCommands = programExecutions.size();

        callOutputListeners(new OutputMessage(String.format("Executing %d commands...", totalCommands), false));

        for(Task programExecution : programExecutions) {
            programExecution.onOutput(this::callOutputListeners);

            programExecution.onExit(exitCode -> {
                invokeCallback(programExitCallbacks, exitCode);

                if(!exitCode.success()) erroredCommands.incrementAndGet();
                executedCommands.incrementAndGet();

                setProgress((float) executedCommands.get() / totalCommands);

                boolean allCommandExecuted = executedCommands.get() == totalCommands;
                if(allCommandExecuted) {
                    callExitListeners(erroredCommands.get() == 0 ? ExitResult.ok() : ExitResult.code(erroredCommands.get()));
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
        programExecutions.forEach(Task::terminate);
    }
}
