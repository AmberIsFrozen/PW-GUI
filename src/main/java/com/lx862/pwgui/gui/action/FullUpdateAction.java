package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.support.packwiz.PackFile;
import com.lx862.pwgui.support.packwiz.PackIndexFile;
import com.lx862.pwgui.support.packwiz.PackwizMetaFile;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.task.BatchedTask;
import com.lx862.pwgui.task.RunProgramTask;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.gui.prompt.IncompatibleSummaryDialog;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * A special task executed after modpack version has changed, used to diagnose mods with incompatible versions.
 */
public class FullUpdateAction extends UpdateAction {
    private final PackFile packFile;

    public FullUpdateAction(Supplier<Window> getParent, PackFile packFile) {
        super(getParent);
        this.packFile = packFile;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Window parent = getParent.get();
        RunProgramTask regularUpdateExecution = getProgramExecution(parent);

        PackIndexFile packIndex = packFile.packIndexFile.get();
        List<PackIndexFile.FileEntry> originalEntries = packIndex.getFileEntries().stream().filter(f -> f.metafile).toList();

        regularUpdateExecution.onExit(exitResult -> {
            if(exitResult.success()) {
                if(!alreadyUpToDate.get() && !modsUpdated.get()) {
                    JOptionPane.showMessageDialog(parent, "Update cancelled, no changes were made.", Util.withTitlePrefix("Update Cancelled!"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    packFile.packIndexFile.clearCache();
                    PackIndexFile newPackIndex = packFile.packIndexFile.get();
                    List<PackIndexFile.FileEntry> unchangedEntries = newPackIndex.getFileEntries().stream()
                            .filter(f -> f.metafile)
                            .filter(f -> originalEntries.stream().anyMatch(g -> Objects.equals(f.hash, g.hash)))
                            .toList();

                    List<PackwizMetaFile> filesWithoutSuitableVersion = new ArrayList<>();

                    Path tempDirectory = packFile.resolveRelative(".pwgui-tmp");

                    try {
                        Files.createDirectories(tempDirectory);
                    } catch (Exception e) {
                        PWGUI.LOGGER.error("", e);
                        JOptionPane.showMessageDialog(parent, "Failed to create a temporary folder to check for version compatibility!\nNote that some content may not have a version that supports the current modloader/minecraft version.", Util.withTitlePrefix("Compatibility checking failed!"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ExecutorService executor = Executors.newFixedThreadPool(1);

                    BatchedTask batchedTask = new BatchedTask("Packwiz", executor);
                    for(PackIndexFile.FileEntry entry : unchangedEntries) {
                        PackwizMetaFile packwizMetaFile = new PackwizMetaFile(entry.path);
                        if(packwizMetaFile.pinned || !packwizMetaFile.haveUpdateSource()) continue;

                        // We ignore github updater for now as there's no reliable way to version check them
                        if(packwizMetaFile.updateGhSlug != null) continue;

                        String prefix = packwizMetaFile.updateMrVersion != null ? "mr" : "cf";
                        RunProgramTask execution;
                        if(prefix.equals("mr")) {
                            execution = PackwizExecutable.INSTANCE.buildCommand("mr", "add", packwizMetaFile.updateMrModId).metaFolder(tempDirectory.getFileName().toString()).build();
                        } else {
                            execution = PackwizExecutable.INSTANCE.buildCommand("cf", "add", "--addon-id", String.valueOf(packwizMetaFile.updateCfProjectId)).metaFolder(tempDirectory.getFileName().toString()).build();
                        }

                        execution.onOutput(stdout -> {
                            if(stdout.isPrompt()) execution.enterInput("N");

                            if(stdout.content().contains("failed to get latest version: no valid versions found") || stdout.content().contains("mod not available for the configured Minecraft version(s)")) {
                                filesWithoutSuitableVersion.add(packwizMetaFile);
                            }
                        });
                        batchedTask.add(execution);

                        // Try installing these mod to a temp directory. If it succeeds, it supports our new modpack configuration!
                        PWGUI.LOGGER.info(entry.file + " is unchanged, adding for check");
                    }

                    batchedTask.onExit(programErrored -> {
                        executor.shutdownNow();
                        PWGUI.LOGGER.info("Found {} incompatible item(s) under the current modpack configuration.", filesWithoutSuitableVersion.size());

                        try {
                            FileUtils.deleteDirectory(tempDirectory.toFile());
                        } catch (Exception e) {
                            PWGUI.LOGGER.error("", e);
                            PWGUI.LOGGER.warn("Failed to remove temporary folder for compatibility check!");
                        }

                        if(filesWithoutSuitableVersion.isEmpty()) {
                            JOptionPane.showMessageDialog(parent, "Everything up to date!", Util.withTitlePrefix("Update Success!"), JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            new IncompatibleSummaryDialog(parent, filesWithoutSuitableVersion).setVisible(true);
                        }

                        PackwizExecutable.INSTANCE.refresh().build().run("Clean-up after content compatibility check");
                    });

                    TaskDialog modCompatDialog = new TaskDialog(parent, "Checking content compatibility...", batchedTask);
                    batchedTask.run("Check content compatibility after update");
                    modCompatDialog.setVisible(true);
                }
            }
        });
        TaskDialog updateProgressDialog = new TaskDialog(parent, "Checking for update...", regularUpdateExecution);
        updateProgressDialog.setVisible(true);
        regularUpdateExecution.run(Strings.REASON_TRIGGERED_BY_USER);
    }
}
