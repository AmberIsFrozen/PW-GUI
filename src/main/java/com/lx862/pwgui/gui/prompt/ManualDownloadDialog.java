package com.lx862.pwgui.gui.prompt;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.model.ManualModInfo;
import com.lx862.pwgui.gui.action.OKAction;
import com.lx862.pwgui.gui.components.fstree.FileSystemWatcher;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.dialog.BaseDialog;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;

public class ManualDownloadDialog extends BaseDialog {
    private final List<ManualModInfo> modList;
    private final JPanel modListPanel;
    private final JLabel watchingPathLabel;
    private final KButton okButton;
    private Path watchingPath;
    private Thread fileWatcherThread;

    public ManualDownloadDialog(JDialog parentDialog, List<ManualModInfo> modList, Consumer<Path> finishCallback) {
        super(parentDialog, Util.withTitlePrefix("Manual Download"), true);
        this.modList = modList;

        setSize(560, 400);
        setLocationRelativeTo(parentDialog);

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel(String.format("%d file(s) to be manually downloaded", modList.size()));
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h3.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel("Due to limitations imposed by API, you need to download the following file(s) manually:");
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        this.modListPanel = new JPanel();
        this.modListPanel.setLayout(new BoxLayout(this.modListPanel, BoxLayout.PAGE_AXIS));
        this.modListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane modListScrollPane = new JScrollPane(this.modListPanel);
        modListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        modListScrollPane.getVerticalScrollBar().setUnitIncrement(10); // Default is too slow
        contentPanel.add(modListScrollPane);

        JLabel watchingDescriptionLabel = new JLabel("We are watching for new files in the following folder:");
        watchingDescriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(watchingDescriptionLabel);

        KGridBagLayoutPanel monitorLocationPanel = new KGridBagLayoutPanel(2, 2);
        monitorLocationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.watchingPathLabel = new JLabel();

        KButton changeWatchPathButton = new KButton("Change...");
        changeWatchPathButton.addActionListener(actionEvent -> {
            KFileChooser fileChooser = new KFileChooser("manual-dl-watcher");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                startWatchDirectory(fileChooser.getSelectedFile().toPath());
            }
        });
        monitorLocationPanel.addRow(1, 0, watchingPathLabel, changeWatchPathButton);

        monitorLocationPanel.addVerticalFiller();

        contentPanel.add(monitorLocationPanel);

        JLabel afterMathLabel = new JLabel("Once all files are downloaded, you can then click \"Continue >\" and we'll take care the rest!");
        afterMathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(afterMathLabel);

        this.okButton = new KButton(new ContinueExportAction("Continue >", finishCallback));
        getRootPane().setDefaultButton(this.okButton);

        KButton cancelButton = new KButton(new AbortAction());

        KActionPanel actionPanel = new KActionPanel.Builder().setPositiveButton(okButton).setNegativeButton(cancelButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(actionPanel);
        add(contentPanel);

        startWatchDirectory(getDefaultDownloadDirectory());
    }

    private void startWatchDirectory(Path path) {
        PWGUI.LOGGER.info("Watching for manually downloaded mods in {}", path);
        if(this.fileWatcherThread != null) {
            this.fileWatcherThread.interrupt();
        }

        this.fileWatcherThread = new Thread(() -> {
            new FileSystemWatcher(path, false, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE).startWatching((parentDir, e) -> {
                SwingUtilities.invokeLater(this::refreshMissingModList);
            });
        });
        this.fileWatcherThread.start();
        this.watchingPathLabel.setText(String.format("<html>Download location: <b>%s</b></html>", path.toString()));
        this.watchingPath = path;
        refreshMissingModList();
    }

    private void refreshMissingModList() {
        this.modListPanel.removeAll();

        boolean allModFound = true;
        for(ManualModInfo manualModInfo : this.modList) {
            boolean modExists = this.watchingPath.resolve(manualModInfo.fileName()).toFile().exists();
            if(!modExists) allModFound = false;

            KListEntryPanel modEntryPanel = new KListEntryPanel();
            JLabel title = new JLabel(String.format("<html><b>%s</b> <span style='color:%s'>%s</span></html>", manualModInfo.name(), modExists ? "green" : "red", modExists ? "(Found!)" : "(Not Found)"));
            title.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h4.font")));
            modEntryPanel.add(title);
            modEntryPanel.add(new JLabel(manualModInfo.fileName()));
            modEntryPanel.add(new KLinkButton(manualModInfo.url()));

            this.modListPanel.add(modEntryPanel);
        }
        this.okButton.setEnabled(allModFound);
        this.modListPanel.updateUI();
    }

    private static Path getDefaultDownloadDirectory() {
        return Paths.get(System.getProperty("user.home")).resolve("Downloads");
    }

    @Override
    public void dispose() {
        super.dispose();
        if(this.fileWatcherThread != null) this.fileWatcherThread.interrupt();
    }

    class ContinueExportAction extends AbstractAction {
        private final Consumer<Path> callback;

        public ContinueExportAction(String title, Consumer<Path> callback) {
            super(title);
            this.callback = callback;
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
            callback.accept(watchingPath);
        }
    }

    class AbortAction extends AbstractAction {
        public AbortAction() {
            super("Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
        }
    }
}
