package com.lx862.pwgui.gui.dialog;

import com.github.rjeschke.txtmark.Processor;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.gui.components.kui.KActionPanel;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.prompt.FileSavedDialog;
import com.lx862.pwgui.pwcore.PackFile;
import com.lx862.pwgui.pwcore.PackIndexFile;
import com.lx862.pwgui.pwcore.PackwizMetaFile;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.gui.panel.editing.filetype.MarkdownPanel;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateModListDialog extends BaseDialog {
    private final PackFile packFile;
    private String plainTextModlist = null;

    private final MarkdownPanel.MarkdownPane previewPane;
    private final JRadioButton plainTextRadioButton;
    private final JRadioButton markdownRadioButton;

    private final JCheckBox projectLinkCheckBox;
    private final JCheckBox versionLinkCheckBox;
    private final JCheckBox fileNameCheckBox;
    private final JCheckBox separateSidesCheckBox;


    public GenerateModListDialog(Window frame, PackFile packFile) {
        super(frame, Util.withTitlePrefix("Generate Modlist"));
        this.packFile = packFile;

        setSize(512, 400);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.PAGE_AXIS));

        JPanel formatRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        formatRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formatLabel = new JLabel("Format:");
        formatRow.add(formatLabel);

        this.plainTextRadioButton = new JRadioButton("Plain Text");
        this.plainTextRadioButton.addItemListener(e -> updateModlist());
        formatRow.add(plainTextRadioButton);

        this.markdownRadioButton = new JRadioButton("Markdown");
        this.markdownRadioButton.setSelected(true);
        this.markdownRadioButton.addItemListener(e -> updateModlist());
        formatRow.add(markdownRadioButton);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(plainTextRadioButton);
        buttonGroup.add(markdownRadioButton);

        headerRow.add(formatRow);

        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        optionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel optionsLabel = new JLabel("Options:");
        optionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsRow.add(optionsLabel);

        this.separateSidesCheckBox = new JCheckBox("Separate Side");
        separateSidesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        separateSidesCheckBox.setSelected(true);
        separateSidesCheckBox.addItemListener(e -> updateModlist());
        optionsRow.add(separateSidesCheckBox);

        this.projectLinkCheckBox = new JCheckBox("Project Link");
        projectLinkCheckBox.setSelected(true);
        projectLinkCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectLinkCheckBox.addItemListener(e -> updateModlist());
        optionsRow.add(projectLinkCheckBox);

        this.versionLinkCheckBox = new JCheckBox("Version Link");
        versionLinkCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionLinkCheckBox.addItemListener(e -> {
            projectLinkCheckBox.setEnabled(!versionLinkCheckBox.isSelected());
            updateModlist();
        });
        optionsRow.add(versionLinkCheckBox);

        this.fileNameCheckBox = new JCheckBox("File Name");
        fileNameCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileNameCheckBox.addItemListener(e -> updateModlist());
        optionsRow.add(fileNameCheckBox);

        headerRow.add(optionsRow);
        contentPanel.add(headerRow, BorderLayout.NORTH);

        this.previewPane = new MarkdownPanel.MarkdownPane();
        JScrollPane previewScrollPane = new JScrollPane(previewPane);
        contentPanel.add(previewScrollPane, BorderLayout.CENTER);

        updateModlist();

        KButton saveAsButton = new KButton(new SaveModlistAction());
        KButton copyButton = new KButton(new CopyModListAction());

        KActionPanel actionPanel = new KActionPanel.Builder().add(saveAsButton, copyButton).build();
        contentPanel.add(actionPanel, BorderLayout.PAGE_END);

        add(contentPanel);
    }

    class SaveModlistAction extends AbstractAction {
        public SaveModlistAction() {
            super("Save As...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            KFileChooser fileChooser = new KFileChooser("generate-modlist", packFile.getPath().getParent());
            fileChooser.setSaveAsFileName(markdownRadioButton.isSelected() ? "modlist.md" : "modlist.txt");
            if (fileChooser.openSaveAsDialog(GenerateModListDialog.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    try(FileWriter fw = new FileWriter(file)) {
                        fw.write(plainTextModlist);
                    }
                    new FileSavedDialog(GenerateModListDialog.this, "Modlist saved!", file).setVisible(true);
                } catch (IOException e) {
                    PWGUI.LOGGER.error("", e);
                    JOptionPane.showMessageDialog(GenerateModListDialog.this, String.format("Failed to save modlist:\n%s\nSee program logs for detail!", e.getMessage()), Util.withTitlePrefix("Save Modlist"), JOptionPane.ERROR_MESSAGE);
                }

                dispose();
            }
        }
    }

    class CopyModListAction extends AbstractAction {
        public CopyModListAction() {
            super("Copy");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Util.copyToClipboard(plainTextModlist);
        }
    }

    private void updateModlist() {
        boolean useMarkdown = markdownRadioButton.isSelected();
        plainTextModlist = getModlist(packFile, useMarkdown, projectLinkCheckBox.isSelected(), versionLinkCheckBox.isSelected(), separateSidesCheckBox.isSelected(), fileNameCheckBox.isSelected());
        previewPane.setInitialContent(useMarkdown ? Processor.process(this.plainTextModlist) : this.plainTextModlist.replace("\n", "<br>"));
    }

    private static String getModlist(PackFile packFile, boolean useMarkdown, boolean projectLink, boolean versionLink, boolean separateSides, boolean showFileName) {
        PackIndexFile indexFile = packFile.packIndexFile.get();
        List<PackwizMetaFile> metaFiles = new ArrayList<>();
        for(PackIndexFile.FileEntry fileEntry : indexFile.getFileEntries()) {
            if(!fileEntry.metafile) continue;
            metaFiles.add(new PackwizMetaFile(packFile.resolveRelative(fileEntry.file)));
        }

        List<PackwizMetaFile> clientFiles = metaFiles.stream().filter(e -> e.isClientSide(true)).toList();
        List<PackwizMetaFile> serverFiles = metaFiles.stream().filter(e -> e.isServerSide(true)).toList();
        List<PackwizMetaFile> bothFiles = metaFiles.stream().filter(e -> e.isClientSide(false) && e.isServerSide(false)).toList();

        StringBuilder sb = new StringBuilder();
        sb.append(useMarkdown ? "# Modlist\n" : "");

        if(!separateSides) {
            for(PackwizMetaFile packwizMetaFile : metaFiles) {
                sb.append(getMetaLine(packwizMetaFile, useMarkdown, projectLink, versionLink, showFileName)).append("\n");
            }
        } else {
            if(!clientFiles.isEmpty()) {
                sb.append("\n").append(getSectionHeader("Client-only", useMarkdown)).append("\n");
                for(PackwizMetaFile packwizMetaFile : clientFiles) {
                    sb.append(getMetaLine(packwizMetaFile, useMarkdown, projectLink, versionLink, showFileName)).append("\n");
                }
            }

            if(!serverFiles.isEmpty()) {
                sb.append("\n").append(getSectionHeader("Server-only", useMarkdown)).append("\n");
                for(PackwizMetaFile packwizMetaFile : serverFiles) {
                    sb.append(getMetaLine(packwizMetaFile, useMarkdown, projectLink, versionLink, showFileName)).append("\n");
                }
            }

            if(!bothFiles.isEmpty()) {
                sb.append("\n").append(getSectionHeader("Common", useMarkdown)).append("\n");
                for(PackwizMetaFile packwizMetaFile : bothFiles) {
                    sb.append(getMetaLine(packwizMetaFile, useMarkdown, projectLink, versionLink, showFileName)).append("\n");
                }
            }
        }

        return sb.toString();
    }

    private static String getSectionHeader(String str, boolean useMarkdown) {
        return useMarkdown ? "## " + str : "===== " + str + " =====";
    }

    private static String getMetaLine(PackwizMetaFile packwizMetaFile, boolean isMarkdown, boolean projectLink, boolean versionLink, boolean showFileName) {
        if(!isMarkdown) {
            String line = packwizMetaFile.name;

            if(packwizMetaFile.optionDescription != null) {
                line += "\n" + packwizMetaFile.optionDescription;
            }

            if(showFileName) {
                line += String.format("\n%s", packwizMetaFile.fileName);
            }

            if(projectLink || versionLink) {
                final String url;
                if(versionLink) {
                    url = packwizMetaFile.getVersionPageURL();
                } else {
                    url = packwizMetaFile.getProjectPageURL();
                }
                line += "\n" + (url == null ? packwizMetaFile.downloadUrl : url);
            }

            line += "\n";
            return line;
        }

        String line = "- ";

        if(versionLink || projectLink) {
            final String url;
            if(versionLink) {
                url = packwizMetaFile.getVersionPageURL();
            } else {
                url = packwizMetaFile.getProjectPageURL();
            }
            line += String.format(("[%s](%s)"), packwizMetaFile.name, (url == null ? packwizMetaFile.downloadUrl : url));
        } else {
            line += String.format("%s", packwizMetaFile.name);
        }


        if(packwizMetaFile.optionDescription != null) {
            line += " - " + (packwizMetaFile.optionDescription).replace("\n", "\n  ");
        }
        if(showFileName) {
            line += String.format(" (%s)", packwizMetaFile.fileName);
        }
        return line;
    }
}
