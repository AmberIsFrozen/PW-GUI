package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.core.Constants;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.components.filter.CurseForgeModpackFilter;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ImportModpackDialog extends BaseDialog {
    public ImportModpackDialog(JFrame parentFrame) {
        super(parentFrame, Util.withTitlePrefix("Import Modpack"), true);

        setSize(300, 400);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImportModpackPanel importModpackPanel = new ImportModpackPanel(this,false, (path) -> {
            JOptionPane.showMessageDialog(this, "Modpack imported successfully!", Util.withTitlePrefix("Import Successful!"), JOptionPane.INFORMATION_MESSAGE);
        });

        add(importModpackPanel);
    }

    public static class ImportModpackPanel extends JPanel {
        private final Window parent;

        public ImportModpackPanel(Window parent, boolean isCreate, Consumer<Path> importCallback) {
            setBorder(GUIHelper.getPaddedBorder(8));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            this.parent = parent;

            JLabel title = new JLabel("Import from CurseForge", SwingConstants.CENTER);
            title.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
            title.setAlignmentX(CENTER_ALIGNMENT);
            add(title);

            JLabel description;
            if(isCreate) {
                 description = new JLabel("<html>" +
                        "<div style='text-align: center;'>" +
                        "<br>" +
                        "<p>You can click the import button below to create a new packwiz modpack from a pre-existing CurseForge modpack</p>" +
                        "<br>" +
                        "<br>" +
                        "</div>" +
                        "</html>", SwingConstants.CENTER);
            } else {
                description = new JLabel("<html>" +
                        "<div style='text-align: center;'>" +
                        "<hr>" +
                        "<br>" +
                        "<p>You can click the import button below to import a pre-existing CurseForge modpack to the current modpack.</p>" +
                        "<br>" +
                        "<br>" +
                        "</div>" +
                        "</html>", SwingConstants.CENTER);
            }
            description.setAlignmentX(CENTER_ALIGNMENT);
            add(description);

            KButton importButton = new KButton("Import...");
            importButton.addActionListener(actionEvent -> importModpack(importCallback, isCreate));
            importButton.setAlignmentX(CENTER_ALIGNMENT);
            add(importButton);
        }

        private void importModpack(Consumer<Path> importCallback, boolean isCreate) {
            KFileChooser cfModpackFileChooser = new KFileChooser("choose-cf-modpack-import");
            cfModpackFileChooser.setFileFilter(new CurseForgeModpackFilter());

            if(cfModpackFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = cfModpackFileChooser.getSelectedFile();

                if(isCreate) {
                    String[] selection = new String[]{"Continue"};
                    JOptionPane.showOptionDialog(this, "Good! Now let's create a folder you want your modpack to be saved t!.", Util.withTitlePrefix("Choose Save Folder"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, selection, selection[0]);

                    KFileChooser modpackFileChooser = new KFileChooser("import-modpack");
                    modpackFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if(modpackFileChooser.openSaveDirectoryDialog(this) == JFileChooser.APPROVE_OPTION) {
                        File destinationPath = modpackFileChooser.getSelectedFile();
                        Executables.packwiz.changeWorkingDirectory(destinationPath.toPath());

                        runImportCommand(file, () -> {
                            importCallback.accept(destinationPath.toPath().resolve("pack.toml"));
                        });
                    }
                } else {
                    runImportCommand(file, () -> importCallback.accept(null));
                }
            }
        }

        private void runImportCommand(File sourceFile, Runnable callback) {
            ProgramExecution programExecution = Executables.packwiz.curseForge().importPack(sourceFile.toString()).build();
            programExecution.onExit(exitCode -> {
                if(exitCode == 0) {
                    callback.run();
                }
            });

            new TaskProgressDialog(parent, "Importing Modpack...", Constants.REASON_TRIGGERED_BY_USER, programExecution).setVisible(true);
        }
    }
}
