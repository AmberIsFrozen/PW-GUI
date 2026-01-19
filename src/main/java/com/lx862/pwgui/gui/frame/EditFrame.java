package com.lx862.pwgui.gui.frame;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.pwcore.Modpack;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.gui.components.fstree.FileSystemWatcher;
import com.lx862.pwgui.gui.panel.editing.HeaderPanel;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;
import com.lx862.pwgui.gui.panel.editing.EditPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/** The main Pack Editing UI */
public class EditFrame extends BaseFrame {
    private final HeaderPanel headerPanel;
    private final EditPanel editPanel;

    private Thread fileWatcherThread;

    public EditFrame(Component parent, Modpack modpack) {
        super(getTitle(modpack));
        setSize(900, 650);
        setLocationRelativeTo(parent);

        PWGUI.getConfig().setLastModpackPath(modpack.getPackFilePath());
        Executables.packwiz.setPackFileLocation(modpack.getRootPath().relativize(modpack.getPackFilePath()).toString());
        Executables.packwiz.changeWorkingDirectory(modpack.getRootPath());

        headerPanel = new HeaderPanel(modpack.packFile.get());
        editPanel = new EditPanel(modpack);


        KRootContentPanel contentPanel = new KRootContentPanel(new BorderLayout(0, 10));
        contentPanel.setBorder(GUIHelper.getPaddedBorder(7, 14, 14, 14));
        registerKeyboardShortcut(contentPanel);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(editPanel, BorderLayout.CENTER);
        add(contentPanel);

        initMenuBars(modpack, editPanel::saveChanges);

        startMonitorModpackDirectory(modpack);
    }

    private static String getTitle(Modpack modpack) {
        return Util.withTitlePrefix(String.format("Editing %s", modpack.packFile.get().name));
    }

    private void initMenuBars(Modpack modpack, Consumer<Boolean> saveChanges) {
        jMenuBar.add(getFileMenu(modpack, saveChanges));
        jMenuBar.add(getEditMenu(modpack));
        jMenuBar.add(getViewMenu());
        jMenuBar.add(getToolMenu(modpack));
        jMenuBar.add(getHelpMenu());
    }

    private void registerKeyboardShortcut(JPanel contentPanel) {
        contentPanel.registerKeyboardAction(actionEvent -> {
            editPanel.saveChanges(false);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void startMonitorModpackDirectory(Modpack modpack) {
        this.fileWatcherThread = new Thread(() -> {
            FileSystemWatcher watcher = new FileSystemWatcher(modpack.getRootPath(), true, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watcher.startWatching((parentDir, e) -> {
                final Path path = parentDir.resolve(((WatchEvent<Path>)e).context());
                WatchEvent.Kind<?> kind = e.kind();

                SwingUtilities.invokeLater(() -> {
                    editPanel.onFileChange(kind, path);

                    if(kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                        if(path.equals(modpack.getPackFilePath())) {
                            modpack.packFile.clearCache();
                            headerPanel.refresh(modpack.packFile.get()); // Update header with new info
                            setTitle(getTitle(modpack));
                        } else if(path.equals(modpack.packFile.get().getIndexPath())) {
                            modpack.packFile.get().packIndexFile.clearCache();
                        }
                    }
                });
            });
        });
        fileWatcherThread.start();
    }

    @Override
    public void dispose() {
        editPanel.saveChanges(true);
        super.dispose();
        fileWatcherThread.interrupt();
        Executables.dispose();
    }
}
