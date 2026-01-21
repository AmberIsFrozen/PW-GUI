package com.lx862.pwgui.gui.prompt;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DownloadProgressDialog extends ProgressDialog {
    private boolean terminateDownload = false;

    public DownloadProgressDialog(Window window, String title, String itemName, URL url, Path destination, DownloadFinishCallback callback) {
        super(window, title);

        PWGUI.LOGGER.info("Downloading {} from {}", itemName, url);
        setStatus(String.format("URL is %s", url.toString()));
        setStatus(String.format("Initiating download for %s...", itemName));

        SwingWorker<Boolean, Long> downloadWorker = new SwingWorker<>() {
            private long contentLength = -1;

            @Override
            protected Boolean doInBackground() throws Exception {
                URLConnection connection = url.openConnection();
                this.contentLength = connection.getContentLength();

                try (BufferedInputStream in = new BufferedInputStream(url.openStream()); FileOutputStream fos = new FileOutputStream(destination.toFile())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long downloaded = 0;
                    while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                        if(terminateDownload) throw new InterruptedException("Download cancelled by user");
                        fos.write(buffer, 0, bytesRead);
                        downloaded += 1024;
                        publish(downloaded);
                    }
                }
                return true;
            }

            @Override
            protected void process(List<Long> chunks) {
                if (contentLength == -1) return;

                int prg = (int) ((chunks.get(chunks.size() - 1) / (float) contentLength) * 100);
                setStatus(String.format("Downloading %s... (%d%%)", itemName, prg));
                DownloadProgressDialog.this.setProgress(prg);
            }

            @Override
            protected void done() {
                try {
                    get();
                    PWGUI.LOGGER.info("Finished downloading {}", itemName);
                    callback.finishedDownloading(true);
                } catch (ExecutionException | InterruptedException e) {
                    PWGUI.LOGGER.error("", e);
                    boolean errorHandled = callback.finishedDownloading(false);

                    if(!errorHandled) {
                        String[] options = new String[]{"Copy URL", "OK"};
                        int result = JOptionPane.showOptionDialog(DownloadProgressDialog.this, String.format("Failed to download %s:\n%s", itemName, e.getMessage()), Util.withTitlePrefix("Download Failed!"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
                        if(result == 0) {
                            Util.copyToClipboard(url.toString());
                        }
                    }
                }

                dispose();
            }
        };
        downloadWorker.execute();
    }

    @Override
    protected void onCancellation() {
        terminateDownload = true;
    }

    public interface DownloadFinishCallback {
        /**
         * Invoked when the download is finished or failed
         * @param success Whether the download succeeded
         * @return If the error have been handled.
         */
        boolean finishedDownloading(boolean success);
    }
}
