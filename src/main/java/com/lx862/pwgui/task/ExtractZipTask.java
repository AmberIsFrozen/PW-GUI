package com.lx862.pwgui.task;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class ExtractZipTask extends Task {
    private final File zipSource;
    private final File destination;
    private final NameMapper nameMapper;

    public ExtractZipTask(String taskName, File zipSource, File destination, NameMapper nameMapper, ExecutorService defaultExecutor) {
        super(taskName, defaultExecutor);
        this.zipSource = zipSource;
        this.destination = destination;
        this.nameMapper = nameMapper;
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        executor.submit(() -> {
            submitOutput(String.format("Extracting file %s into %s", zipSource.getName(), destination.getName()));
            try {
                ZipUtil.unpack(zipSource, destination, nameMapper);
                submitExitResult(ExitResult.ok());
            } catch (ZipException zipException) {
                submitOutput(String.format("Failed to extract file %s!", zipSource.getName()));
                submitExitResult(ExitResult.exception(999, zipException));
            }
        });
    }

    @Override
    public void terminate() {
    }
}
