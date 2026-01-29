package com.lx862.pwgui.task;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.NetworkHelper;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HttpFetchTask extends Task {
    private final ConstructRequest constructRequest;
    private final Consumer<String> stringConsumer;

    public HttpFetchTask(String taskName, ConstructRequest constructRequest, Consumer<String> stringConsumer) {
        super(taskName, PWGUI.BACKGROUND_EXECUTOR);
        this.stringConsumer = stringConsumer;
        this.constructRequest = constructRequest;
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        executor.submit(() -> {
            try {
                HttpURLConnection urlConnection = constructRequest.construct();
                stringConsumer.accept(NetworkHelper.readString(urlConnection.getInputStream()));
                submitExitResult(ExitResult.ok());
            } catch (Exception e) {
                PWGUI.LOGGER.error("Failed to perform fetch task {}.", e, getTaskName());
                submitExitResult(ExitResult.exception(-1, e));
            }
        });
    }

    @Override
    public void terminate() {
    }

    @FunctionalInterface
    public interface ConstructRequest {
       HttpURLConnection construct() throws Exception;
    }
}
