package com.lx862.pwgui.task;

import com.lx862.pwgui.PWGUI;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class ExtractZipTask extends Task {
    private final File zipSource;
    private final File destination;
    private final NameMapper nameMapper;

    public ExtractZipTask(String taskName, File zipSource, File destination, NameMapper nameMapper) {
        super(taskName, PWGUI.BACKGROUND_EXECUTOR);
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

    public static class InnerDirectory implements NameMapper {
        private final String dirName;

        public InnerDirectory(String dirName) {
            this.dirName = dirName + "/";
        }

        @Override
        public String map(String s) {
            return s.startsWith(dirName) ? s.substring(dirName.length()) : null;
        }
    }
}
