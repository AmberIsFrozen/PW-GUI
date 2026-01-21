package com.lx862.pwgui.util;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.Main;
import com.lx862.pwgui.core.BuildMetadata;
import com.lx862.pwgui.core.data.model.ManualModInfo;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.gui.prompt.ManualDownloadDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final Pattern MANUAL_DOWNLOAD_FILENAME_PATTERN = Pattern.compile("\\(([^()]*|\\((?:[^()]*|\\([^()]*\\))*\\))*\\)");
    /** Returns a String with the format "PROGRAM_NAME - TEXT" */
    public static String withTitlePrefix(String str) {
        return String.format("%s - %s", BuildMetadata.INSTANCE.name, str);
    }

    /** Returns a String with the format "[PROGRAM_NAME] TEXT" */
    public static String withBracketPrefix(String str) {
        return String.format("[%s] %s", BuildMetadata.INSTANCE.name, str);
    }

    /** Get resources from jar */
    public static InputStream getAssets(String path) {
        return Main.class.getResourceAsStream(path);
    }

    /** Copy resources from within a jar to a file in a directory */
    public static void copyAssetsToPath(String assetsPath, String outputName, Path path) throws IOException {
        try(InputStream is = getAssets(assetsPath)) {
            Files.copy(is, path.resolve(outputName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Copy a string to the system clipboard */
    public static void copyToClipboard(String str) {
        StringSelection stringSelection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /** Replaces all backward slashes with forward slashes in a path
    * Toml4j has a issue of misparsing \UXXX as a unicode code points, so something like C:\Users on Windows could break the parsing entirely */
    public static String toForwardSlashString(Path path) {
        if(File.pathSeparator.equals("\\")) {
            return path.toString().replaceAll(Matcher.quoteReplacement("\\"), "/");
        } else {
            return path.toString();
        }
    }

    /**
     * Check whether a path is subdirectory of a root directory
     * @param root The root directory
     * @param path The path to check against
     * @return Returns true if path is a subdirectory of root, false if it is outside the root directory.
     */
    public static boolean withinDirectory(Path root, Path path) {
        File parent = path.normalize().toFile();
        boolean isWithinDirectory = false;

        while(parent != null) {
            if(parent.equals(root.toFile())) {
                isWithinDirectory = true;
                break;
            }
            parent = parent.getParentFile();
        }

        return isWithinDirectory;
    }

    public static void tryOpenFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(null, String.format("Failed to open file \"%s\":\n%s", file.getName(), e.getMessage()), withTitlePrefix("Open External Application"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void tryBrowse(String uri) {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (UnsupportedOperationException e) {
            // Linux fallback
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"xdg-open", uri});
                process.waitFor();
            } catch (Exception ex) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(null, String.format("Failed to open link \"%s\" with xdg-open:\n%s", uri, e.getMessage()), withTitlePrefix("Open External Link"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException | IOException e) {
            PWGUI.LOGGER.error("", e);
            JOptionPane.showMessageDialog(null, String.format("Failed to open link \"%s\":\n%s", uri, e.getMessage()), withTitlePrefix("Open External Link"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void addManualDownloadPrompt(JDialog parentDialog, ProgramExecution programExecution, TaskProgressDialog taskProgressDialog, Runnable callback) {
        List<ManualModInfo> manualDownloadMod = new ArrayList<>();
        AtomicBoolean captureManualDownloadMod = new AtomicBoolean();
        AtomicReference<String> cachePath = new AtomicReference<>();

        programExecution.onOutput((stdout) -> {
            String line = stdout.content();
            if(line.contains("and must be manually downloaded")) {
                captureManualDownloadMod.set(true);
            } else if(line.contains("Once you have done so, place")) {
                cachePath.set(line.split(", place these files in ")[1].split(" and re-run")[0]);
                new ManualDownloadDialog(parentDialog, manualDownloadMod, (path) -> {
                    moveManualDownloadToCache(parentDialog, manualDownloadMod, path, Paths.get(cachePath.get()));
                    callback.run();
                }).setVisible(true);
            } else if(captureManualDownloadMod.get()) {
                // Format: Mod Display Name (mod_file_name.jar) from https://example.com

                String[] split = line.split(" from http");
                String metaNameAndFileName = split[0];
                String url = "http" + split[1];

                String fileName = "(pwgui_cannot_parse_filename.jar)";
                Matcher fileNameMatcher = MANUAL_DOWNLOAD_FILENAME_PATTERN.matcher(metaNameAndFileName);

                while(fileNameMatcher.find()) {
                    fileName = fileNameMatcher.group();
                }

                fileName = fileName.substring(1, fileName.length()-1);
                String displayName = line.substring(0, line.indexOf(fileName)-1);

                manualDownloadMod.add(new ManualModInfo(displayName, fileName, url));
            }
        });

        if(taskProgressDialog != null) {
            taskProgressDialog.whenProgramErrored(() -> !captureManualDownloadMod.get()); // Mute exit code 1 error pop up if it's about manual download
        }
    }

    private static void moveManualDownloadToCache(Component parent, List<ManualModInfo> modList, Path sourceDirectory, Path cacheDirectory) {
        for(ManualModInfo modInfo : modList) {
            Path sourcePath = sourceDirectory.resolve(modInfo.fileName());
            Path destinationPath = cacheDirectory.resolve(modInfo.fileName());
            try {
                PWGUI.LOGGER.info("Moving manually downloaded file from \"{}\" to \"{}\"", sourcePath, destinationPath);
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(parent, String.format("An error occured while moving manually downloaded files:\n%s\nPlease move %s manually to %s", e.getMessage(), modInfo.fileName(), cacheDirectory), Util.withTitlePrefix("Failed to Move Files!"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
