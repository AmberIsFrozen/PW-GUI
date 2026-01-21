package com.lx862.pwgui.gui.panel.editing.filetype.content;

import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.pwcore.Modpack;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.gui.prompt.NumericSelectionDialog;
import com.lx862.pwgui.gui.panel.editing.filetype.FileEntryPaneContext;
import com.lx862.pwgui.gui.panel.editing.filetype.FileTypePanel;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.pwcore.data.IconNamePair;
import com.lx862.pwgui.core.data.model.file.ContentDirectoryModel;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AddContentPanel extends FileTypePanel {
    public AddContentPanel(FileEntryPaneContext context, ContentDirectoryModel fileEntry) {
        super(context);
        setLayout(new BorderLayout());

        JTabbedPane tab = new JTabbedPane();
        tab.addTab(IconNamePair.MODRINTH.name, new ImageIcon(GUIHelper.clampImageSize(IconNamePair.MODRINTH.image, 20)), new ModrinthPanel(context, fileEntry));
        tab.addTab(IconNamePair.CURSEFORGE.name, new ImageIcon(GUIHelper.clampImageSize(IconNamePair.CURSEFORGE.image, 20)), new CurseForgePanel(context, fileEntry));
        tab.addTab("URL Link", new ImageIcon(GUIHelper.convertImage(Util.getAssets("/assets/ui/mime/link.png"), 20)), new UrlPanel(context, fileEntry));
        add(tab);
    }

    public static void addProjectFromContentPlatform(Window parent, Modpack modpack, String... args) {
        ProgramExecution programExecution = Executables.packwiz.buildCommand(args).build();
        TaskProgressDialog dialog = new TaskProgressDialog(parent, "Adding mod...", Strings.REASON_TRIGGERED_BY_USER, programExecution);

        List<String> recordedOutputs = new ArrayList<>();

        AtomicReference<String> modName = new AtomicReference<>(null);
        AtomicBoolean recordOutput = new AtomicBoolean();
        AtomicBoolean noValidVersion = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();

        programExecution.onOutput((stdout) -> {
            String line = stdout.content();
            if((line.startsWith("Searching") && line.endsWith("...")) || line.startsWith("Dependencies found:")) {
                recordedOutputs.clear();
                recordOutput.set(true);
                return;
            }

            if(line.startsWith("0) Cancel")) { // We have a GUI cancel button, we don't need this entry
                return;
            }

            if(line.equals("Cancelled!") || line.equals("Failed to add project: project selection cancelled")) {
                cancelled.set(true);
            }

            if(line.startsWith("Failed to add project: failed to get latest version")) {
                noValidVersion.set(true);
            }

            if(line.startsWith("Choose a number:")) {
                recordOutput.set(false);
                new NumericSelectionDialog(dialog, "Select Mod", recordedOutputs, (selectIdx) -> {
                    if(selectIdx == -1) {
                        programExecution.enterInput("0");
                    } else {
                        programExecution.enterInput(String.valueOf(selectIdx + 1));
                    }
                }).setVisible(true);
            }

            // Dependencies
            if(line.endsWith("Would you like to add them? [Y/n]: ")) {
                String depList = recordedOutputs.stream().map(e -> "• " + e).collect(Collectors.joining("\n"));
                if(JOptionPane.showConfirmDialog(dialog, String.format("The following dependencies are required:\n%s\nDo you want to add them?", depList), Util.withTitlePrefix("Add Dependencies?"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    programExecution.enterInput("Y");
                } else {
                    programExecution.enterInput("N");
                }
            }

            // Mod name
            if(line.contains("Project \"") && line.contains("\" successfully added!")) {
                String croppedModName = line.split("Project ")[1].split(" successfully added!")[0];
                String unquotedModName = croppedModName.substring(1, croppedModName.length()-1);
                modName.set(unquotedModName);
            }

            // Record log
            if(recordOutput.get()) {
                boolean isNumericChoice = line.contains(") ");
                String processedLine =  isNumericChoice ? line.substring(line.indexOf(") ")+2) : line;
                recordedOutputs.add(processedLine);
            }
        });

        dialog.whenProgramErrored(() -> {
            if(noValidVersion.get()) {
                List<String> tips = new ArrayList<>();
                tips.add("- You may need to configure \"Acceptable Minecraft Version\" in Modpack Config if a cross-compatible version (i.e. 1.20 & 1.20.1) is available.");

                if(modpack.packFile.get().getDatapackPath() == null) {
                    tips.add("- If you are trying to install a Datapack, you need to configure the Datapack folder in Modpack Config.");
                }
                JOptionPane.showMessageDialog(parent, String.format("No valid version found!\nThis likely means the project does not have a version that is compatible with your modloader/Minecraft version.\n\nTips:\n%s", String.join("\n", tips)), Util.withTitlePrefix("No Valid Version Found!"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        });

        programExecution.onExit((exitCode) -> {
            if(exitCode == 0 && !cancelled.get()) {
                JOptionPane.showMessageDialog(parent, String.format("%s has been added to the modpack!", modName.get()), Util.withTitlePrefix("Project Added!"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}
