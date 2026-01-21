package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.pwcore.PackIndexFile;
import com.lx862.pwgui.core.data.model.file.GenericFileModel;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;

public class FilePanel extends FileTypePanel {
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("####.#");
    private final PackIndexFile indexFile;
    private final PackIndexFile.FileEntry indexEntry;
    private JCheckBox preserveCheckBox;
    private boolean initialPreserve;

    public FilePanel(FileEntryPaneContext context, GenericFileModel fileEntry) {
        super(context);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel fileNameLabel = new JLabel(String.format("File name: %s", fileEntry.name));
        fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(fileNameLabel);

        JLabel fileSizeLabel = new JLabel(String.format("File size: %s", formatFileSize(fileEntry.fileSize)));
        fileSizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(fileSizeLabel);

        this.indexFile = context.getModpack().packFile.get().packIndexFile.get();
        this.indexEntry = context.getModpack().packFile.get().packIndexFile.get().getEntryByPath(fileEntry.path);

        if(indexEntry != null) {
            if(indexEntry.hash != null) { // If no-internal-hashes is set to true, then there won't be a hash
                JLabel hashLabel = new JLabel(String.format("Hash (%s): %s", indexEntry.hashFormat, indexEntry.hash));
                hashLabel.setToolTipText(indexEntry.hash);
                hashLabel.setMinimumSize(new Dimension(1, 1)); // Hash is too long
                hashLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(hashLabel);
            }

            initialPreserve = indexEntry.preserve;

            preserveCheckBox = new JCheckBox("Preserve changes made by player");
            preserveCheckBox.addActionListener(e -> updateSaveState());
            preserveCheckBox.setSelected(indexEntry.preserve);
            preserveCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(preserveCheckBox);
        }

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        KButton openFileButton = new KButton("Open file");
        openFileButton.setMnemonic(KeyEvent.VK_O);
        openFileButton.addActionListener(actionEvent -> Util.tryOpenFile(fileEntry.path.toFile()));

        actionButtons.add(openFileButton);

        KButton removeFileButton = new KButton("Remove file");
        removeFileButton.setMnemonic(KeyEvent.VK_R);
        removeFileButton.addActionListener(actionEvent -> {
            final boolean shouldDelete;
            if(context.getModpack().isCriticalFile(fileEntry.path)) {
                Object[] options = new String[]{"Yes", "No"};
                shouldDelete = JOptionPane.showOptionDialog(getTopLevelAncestor(), "Removing this critical file would render the modpack unusable.\nOnly continue if you know what you are doing!", Util.withTitlePrefix("Delete Confirmation"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]) == JOptionPane.YES_OPTION;
            } else {
                shouldDelete = JOptionPane.showConfirmDialog(getTopLevelAncestor(), String.format("Are you sure you want to delete \"%s\"?", fileEntry.name), Util.withTitlePrefix("Delete Confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }

            if(shouldDelete) {
                try {
                    Files.delete(fileEntry.path);
                    PWGUI.LOGGER.info("Deleted file {}", fileEntry.path);
                    Executables.packwiz.refresh().build().run("File deleted by user");
                } catch (IOException e) {
                    PWGUI.LOGGER.error("Failed to deleted file {} due to {}", fileEntry.path, e.getMessage());
                    JOptionPane.showMessageDialog(getTopLevelAncestor(), String.format("Failed to delete file: \n%s\nYou may try doing it from an external file manager.", e.getMessage()), Util.withTitlePrefix("Failed to Delete File!"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        actionButtons.add(removeFileButton);

        add(actionButtons);
    }

    @Override
    public boolean savable() {
        return preserveCheckBox != null;
    }

    @Override
    public boolean shouldSave() {
        return preserveCheckBox != null && initialPreserve != preserveCheckBox.isSelected();
    }

    @Override
    public void save() throws IOException {
        if(indexEntry != null) {
            super.save();
            indexEntry.preserve = preserveCheckBox.isSelected();
            indexFile.updateEntry(indexEntry);
            indexFile.write(Strings.REASON_TRIGGERED_BY_USER);
            initialPreserve = indexEntry.preserve;
        }
    }

    public static String formatFileSize(long bytes) {
        if(bytes < 1024) return bytes + " B";

        double kib = bytes / 1024.0;
        if(kib < 1024) return SIZE_FORMAT.format(kib) + " KiB";
        double mib = kib / 1024.0;
        if(mib < 1024) return SIZE_FORMAT.format(mib) + " MiB";

        double gib = mib / 1024.0;
        return SIZE_FORMAT.format(gib) + "GiB";
    }
}
