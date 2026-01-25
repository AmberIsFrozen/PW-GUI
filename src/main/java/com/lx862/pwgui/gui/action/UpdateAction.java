package com.lx862.pwgui.gui.action;

import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.task.RunProgramTask;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.gui.prompt.UpdateSummaryDialog;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class UpdateAction extends AbstractAction {
    protected final Supplier<Window> getParent;
    protected AtomicBoolean alreadyUpToDate = new AtomicBoolean();
    protected AtomicBoolean modsUpdated = new AtomicBoolean();

    public UpdateAction(Supplier<Window> getParent) {
        super("Update All Items");
        this.getParent = getParent;
        putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window parent = getParent.get();
        RunProgramTask runProgramTask = getProgramExecution(parent);

        runProgramTask.onExit(exitResult -> {
            if(exitResult.success()) {
                if(alreadyUpToDate.get()) {
                    JOptionPane.showMessageDialog(parent, "All files are already up to date!", Util.withTitlePrefix("Up to Date!"), JOptionPane.INFORMATION_MESSAGE);
                } else if(modsUpdated.get()) {
                    JOptionPane.showMessageDialog(parent, "All files have been updated!", Util.withTitlePrefix("Update Successful!"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, "Update cancelled, no changes were made.", Util.withTitlePrefix("Update Cancelled!"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        TaskDialog taskDialog = new TaskDialog(parent, "Checking for update...", runProgramTask);
        runProgramTask.run(Strings.REASON_TRIGGERED_BY_USER);
        taskDialog.setVisible(true);
    }

    public RunProgramTask getProgramExecution(Window parent) {
        RunProgramTask runProgramTask = PackwizExecutable.INSTANCE.updateAll().build();
        List<String> updateMods = new ArrayList<>();
        List<String> skippedMods = new ArrayList<>();
        List<String> unsupportedMods = new ArrayList<>();
        AtomicBoolean startLogMods = new AtomicBoolean();

        runProgramTask.onOutput(stdout -> {
            String line = stdout.content();
            if(line.startsWith("Updates found:")) {
                startLogMods.set(true);
            }

            if(line.startsWith("A supported update system for") && line.endsWith("cannot be found.")) {
                unsupportedMods.add(line.split("A supported update system for \"")[1].split("\" cannot be found\\.")[0]);
            }

            if(line.startsWith("Update skipped for pinned mod")) {
                skippedMods.add(line.split("Update skipped for pinned mod ")[1]);
            }

            if(line.equals("Do you want to update? [Y/n]: ")) {
                new UpdateSummaryDialog(parent, updateMods, skippedMods, unsupportedMods, (update) -> {
                    if(update) runProgramTask.enterInput("Y");
                    else runProgramTask.enterInput("N");
                }).setVisible(true);
            }

            if(line.equals("All files are up to date!")) {
                alreadyUpToDate.set(true);
            }

            if(line.equals("Do you want to update? [Y/n]: Files updated!")) {
                modsUpdated.set(true);
            }

            if(startLogMods.get() && line.contains(" -> ")) {
                updateMods.add(line);
            }
        });
        return runProgramTask;
    }
}
