package com.lx862.pwgui.task;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTask extends Task {
    private final String itemName;
    private final URL url;
    private final Path destinationDir;
    private boolean terminateDownload = false;

    public DownloadTask(String taskName, String itemName, URL url, Path destinationDir) {
        super(taskName, PWGUI.BACKGROUND_EXECUTOR);
        this.itemName = itemName;
        this.url = url;
        this.destinationDir = destinationDir;
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        submitOutput(new OutputMessage(String.format("Downloading %s from %s", itemName, url.toString()), false));
        submitOutput(new OutputMessage(String.format("Initiating download for %s...", itemName), false));

        executor.submit(() -> {
            try {
                URLConnection connection = url.openConnection();
                int contentLength = connection.getContentLength();

                try (BufferedInputStream in = new BufferedInputStream(url.openStream()); FileOutputStream fos = new FileOutputStream(destinationDir.toFile())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long downloaded = 0;
                    while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                        if(terminateDownload) throw new InterruptedException("Download cancelled by user");
                        fos.write(buffer, 0, bytesRead);
                        downloaded += 1024;
                        float progress = (float)downloaded / contentLength;
                        submitProgress(progress);
                        submitOutput(new OutputMessage(String.format("Downloading %s (%d%%)", itemName, (int)(progress * 100)), false));
                    }
                }
                submitExitResult(ExitResult.ok());
            } catch (InterruptedException e) {
                submitExitResult(ExitResult.terminated(e));
            } catch (Exception e) {
                PWGUI.LOGGER.error("Failed to download {}!", e, itemName);
                submitExitResult(ExitResult.exception(-1, e));
            }
        });
    }

    @Override
    public void terminate() {
        terminateDownload = true;
    }

    public void showErrorDialog(Window parent, Exception ex) {
        String[] options = new String[]{"Copy URL", "OK"};
        int result = JOptionPane.showOptionDialog(parent, String.format("Failed to download %s:\n%s: %s", getTaskName(), ex.getClass().getName(), ex.getMessage()), Util.withTitlePrefix("Download Failed!"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
        if(result == 0) {
            Util.copyToClipboard(getUrl().toString());
        }
    }

    public URL getUrl() {
        return this.url;
    }
}
