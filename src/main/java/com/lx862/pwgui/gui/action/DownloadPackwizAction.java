package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.task.DownloadTask;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadPackwizAction extends AbstractAction {
    private final Window parent;
    private final Consumer<Path> finishCallback;
    private static final String[] downloadMirror = {
            "https://nightly.link/packwiz/packwiz/workflows/go/main/%s.zip", // https://github.com/packwiz/packwiz/tree/main
            "https://github.com/AmberIsFrozen/packwiz/releases/latest/download/%s.zip" // https://github.com/AmberIsFrozen/packwiz/tree/artifact
    };

    public DownloadPackwizAction(String title, Window parent, Consumer<Path> finishCallback) {
        super(title);
        this.parent = parent;
        this.finishCallback = finishCallback;
        putValue(MNEMONIC_KEY, KeyEvent.VK_D);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("pwgui");
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(parent, "Failed to create temporary folder, see program logs for detail!", Util.withTitlePrefix("Download Failed"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            attemptDownload(downloadMirror, 0, parent, tempDirectory);
        } catch (MalformedURLException ignored) {
        }
    }

    private void attemptDownload(String[] mirrors, int mirrorIdx, Window parent, Path destinationPath) throws MalformedURLException {
        Path destination = destinationPath.resolve("packwiz_executable.zip");
        String urlStr = mirrors[mirrorIdx];
        URL url = URI.create(String.format(urlStr, getArtifactName(urlStr.startsWith("https://nightly.link")))).toURL();
        boolean lastAvailableAttempt = mirrorIdx == mirrors.length-1;

        DownloadTask task = new DownloadTask("packwiz", "packwiz", url, destination);
        TaskDialog downloadDialog = new TaskDialog(parent, "Downloading packwiz...", task);

        task.onExit(exitResult -> {
            downloadDialog.dispose();
            if(exitResult.terminated()) {
                return;
            }

            if(lastAvailableAttempt && !exitResult.success()) {
                task.showErrorDialog(parent, exitResult.exception());
                return;
            }

            if(!exitResult.success()) {
                PWGUI.LOGGER.error("Failed to download from " + url + "!");
                int newAttemptedMirror = mirrorIdx+1;
                if(newAttemptedMirror < mirrors.length) {
                    try {
                        JOptionPane.showMessageDialog(parent, String.format("Failed to download from %s\nWill retry with another mirror.", url), Util.withTitlePrefix("Download Failed"), JOptionPane.WARNING_MESSAGE);
                        attemptDownload(mirrors, newAttemptedMirror, parent, destinationPath);
                    } catch (MalformedURLException ignored) {
                    }
                }
            } else {
                Path packwizExecutablePath = null;

                Path binDirectory = Config.CONFIG_DIR_PATH.resolve("bin");
                try {
                    Files.createDirectories(binDirectory);
                } catch (IOException e) {
                    PWGUI.LOGGER.error("", e);
                    JOptionPane.showMessageDialog(parent, "Failed to create the folder containing the packwiz executable, see program logs for detail!", Util.withTitlePrefix("Setup Failed"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try(FileInputStream fis = new FileInputStream(destination.toFile()); ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry zipEntry;
                    while((zipEntry = zis.getNextEntry()) != null) {
                        if(zipEntry.getName().equals("packwiz") || zipEntry.getName().equals("packwiz.exe")) {
                            packwizExecutablePath = binDirectory.resolve(zipEntry.getName());
                            FileUtils.copyToFile(zis, packwizExecutablePath.toFile());
                            break;
                        }
                    }
                } catch (Exception e) {
                    PWGUI.LOGGER.error("", e);
                    JOptionPane.showMessageDialog(parent, String.format("Failed to extract packwiz from zip file:\n%s\nSee program logs for detail!", e.getMessage()), Util.withTitlePrefix("Setup Failed"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Files.delete(destination);
                    Files.delete(destinationPath);
                } catch (IOException ignored) { // Not important
                }

                boolean configureSuccessful = configurePackwiz(packwizExecutablePath);
                if(configureSuccessful) {
                    finishCallback.accept(packwizExecutablePath);
                }
            }
        });

        task.run(Strings.REASON_TRIGGERED_BY_USER);
        downloadDialog.setVisible(true);
    }

    private static String getArtifactName(boolean isArtifact) {
        boolean isWindows = false;
        ApplicationInfo.OperatingSystem os = ApplicationInfo.INSTANCE.os;

        String artifactName;
        switch(os.type()) {
            case WINDOWS -> {
                isWindows = true;
                artifactName = "Windows 64-bit";
            }
            case MAC_OS -> {
                artifactName = "macOS 64-bit";
            }
            case LINUX -> {
                artifactName = "Linux 64-bit";
            }
            default -> {
                throw new IllegalStateException("Unknown operating system, unable to download.");
            }
        }

        if(ApplicationInfo.INSTANCE.os.architecture().isArm()) artifactName += " ARM";
        if(ApplicationInfo.INSTANCE.os.architecture().isRiscV()) artifactName += " RISC-V";
        else if(!isWindows) artifactName += " x86";

        artifactName = artifactName.replace(" ", isArtifact ? "%20" : ".");
        return artifactName;
    }

    private boolean configurePackwiz(Path path) {
        Config.getInstance().packwizExecutablePath.setValue(path);

        try {
            try { // We want executable permission on *nix
                Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-----"));
            } catch (UnsupportedOperationException ignored) {
            }

            PackwizExecutable.INSTANCE.updateExecutableLocation(null);
            if(!PackwizExecutable.INSTANCE.usable()) {
                JOptionPane.showMessageDialog(parent, "Packwiz executable is not valid :(\nPlease try manually downloading packwiz and locating it.", Util.withTitlePrefix("Invalid Executable"), JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Config.getInstance().write("Update packwiz executable path");
            return true;
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(parent, "Failed to save config file, see program logs for detail!", Util.withTitlePrefix("Setup Failed"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }
}
