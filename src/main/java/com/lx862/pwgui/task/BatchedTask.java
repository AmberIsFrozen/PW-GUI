package com.lx862.pwgui.task;

import com.lx862.pwgui.PWGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Executes multiple tasks and invokes callback after completion of all commands */
public class BatchedTask extends Task {
    private final List<Task> tasks;
    private boolean started = false;

    public BatchedTask(String taskName) {
        this(taskName, PWGUI.BACKGROUND_EXECUTOR);
    }

    public BatchedTask(String taskName, ExecutorService executorService) {
        super(taskName, executorService);
        this.tasks = new ArrayList<>();
    }

    /** Add another program to queued for execution */
    public void add(Task task) {
        if(started) throw new IllegalStateException("No more task should be added after task has been started!");
        tasks.add(task);
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        started = true;
        if(tasks.isEmpty()) { // Nothing to run
            submitExitResult(ExitResult.ok());
            return;
        }

        AtomicInteger erroredTasks = new AtomicInteger();
        AtomicInteger executedTasks = new AtomicInteger();
        int totalTasks = tasks.size();

        submitOutput(new OutputMessage(String.format("Executing %d commands...", totalTasks), false));

        for(Task task : tasks) {
            task.onOutput(this::submitOutput);

            task.onExit(exitResult -> {
                if(!exitResult.success() && !exitResult.terminated()) erroredTasks.incrementAndGet();
                executedTasks.incrementAndGet();

                submitProgress((float) executedTasks.get() / totalTasks);

                boolean allTaskExecuted = executedTasks.get() == totalTasks;
                if(allTaskExecuted) {
                    submitExitResult(erroredTasks.get() == 0 ? ExitResult.ok() : ExitResult.code(erroredTasks.get()));
                }
            });
            task.run(reason, executor);
        }
    }

    @Override
    public void terminate() {
        tasks.forEach(Task::terminate);
    }
}
