package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemFileChooser;
import com.lx862.pwgui.gui.components.AlignedBoxPanel;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.gui.components.kui.KRootContentPanel;
import com.lx862.pwgui.gui.prompt.TaskDialog;
import com.lx862.pwgui.gui.prompt.FileSavedDialog;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Strings;
import com.lx862.pwgui.support.packwiz.data.IconNamePair;
import com.lx862.pwgui.gui.components.kui.KFileChooser;
import com.lx862.pwgui.task.RunProgramTask;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExportModpackDialog extends BaseDialog {
    private final KButton exportButton;

    public ExportModpackDialog(JFrame parentFrame, Modpack modpack) {
        super(parentFrame, Util.withTitlePrefix("Export Modpack"), true);

        setSize(380, 300);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        KRootContentPanel contentPanel = new KRootContentPanel(10);

        JLabel titleLabel = new JLabel("Export Modpack");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane formatTabPane = new JTabbedPane();
        formatTabPane.setBorder(GUIConfiguration.getPaddedBorder(10, 0, 10, 0));
        formatTabPane.addTab(IconNamePair.MODRINTH.name(), new ImageIcon(ImageUtil.clampImageSize(IconNamePair.MODRINTH.image(), 20)), new ModrinthExportPanel());
        formatTabPane.addTab(IconNamePair.CURSEFORGE.name(), new ImageIcon(ImageUtil.clampImageSize(IconNamePair.CURSEFORGE.image(), 20)), new CurseforgeExportPanel(this::setExportButtonState));

        formatTabPane.addChangeListener(changeEvent -> {
            ExportPanel selectedTab = (ExportPanel)formatTabPane.getComponentAt(formatTabPane.getSelectedIndex());
            setExportButtonState(selectedTab.canExport());
        });

        exportButton = new KButton("Export!");
        exportButton.addActionListener(actionEvent -> {
            ExportPanel selectedTab = (ExportPanel)formatTabPane.getComponentAt(formatTabPane.getSelectedIndex());

            KFileChooser fileChooser = new KFileChooser("export-modpack");
            fileChooser.setFileFilter(new SystemFileChooser.FileNameExtensionFilter("Modpack file", selectedTab.getExtension().substring(1)));
            fileChooser.setDialogTitle("Choose Modpack Saving Location");
            fileChooser.setSaveAsFileName(modpack.packFile.get().name + selectedTab.getExtension());
            if(fileChooser.openSaveAsDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                List<String> args = new ArrayList<>(selectedTab.getArguments());
                args.add("--output");
                args.add(file.getPath());

                exportModpack(args, file);
            }
        });

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.add(exportButton);

        contentPanel.add(actionRow, BorderLayout.SOUTH);

        contentPanel.add(formatTabPane);
        add(contentPanel);
    }

    private void exportModpack(List<String> args, File destination) {
        RunProgramTask programRefresh = PackwizExecutable.INSTANCE.refresh().build();
        programRefresh.onExit(refreshExitResult -> {
            if(!refreshExitResult.success()) return;

            RunProgramTask program = PackwizExecutable.INSTANCE.buildCommand(args.toArray(new String[0])).build();
            TaskDialog dialog = new TaskDialog(this, "Exporting Modpack...", program);
            Util.addManualDownloadPrompt(this, program, dialog, () -> {
                exportModpack(args, destination);
            });
            program.onExit(exitResult -> {
                if(exitResult.success()) {
                    new FileSavedDialog(this, "Modpack Exported!", destination).setVisible(true);
                }
            });
            program.run(Strings.REASON_TRIGGERED_BY_USER);
            dialog.setVisible(true);
        });

        TaskDialog taskDialog = new TaskDialog(this, "Refreshing Modpack...", programRefresh);
        programRefresh.run("Refresh before export to ensure consistency.");
        taskDialog.setVisible(true);
    }

    private void setExportButtonState(boolean active) {
        exportButton.setEnabled(active);
    }
}

class ModrinthExportPanel extends AlignedBoxPanel implements ExportPanel {
    private final JCheckBox restrictDomainCheckBox;

    public ModrinthExportPanel() {
        super(LEFT_ALIGNMENT);
        JLabel formatLabel = new JLabel("<html>Format: <b>" + getExtension() + "</b></html>");
        formatLabel.setBorder(GUIConfiguration.getPaddedBorder(4, 0, 4, 0));
        add(formatLabel);

        restrictDomainCheckBox = new JCheckBox("Restricts domains to those allowed by modrinth.com");
        restrictDomainCheckBox.setSelected(true);

        add(restrictDomainCheckBox);
    }

    @Override
    public List<String> getArguments() {
        List<String> args = new ArrayList<>();
        args.add("modrinth");
        args.add("export");
        args.add("--restrictDomains=" + (restrictDomainCheckBox.isSelected() ? "true" : "false"));
        return args;
    }

    @Override
    public boolean canExport() {
        return true;
    }

    @Override
    public String getExtension() {
        return ".mrpack";
    }
}

class CurseforgeExportPanel extends AlignedBoxPanel implements ExportPanel {
    private final JCheckBox exportClientCheckBox;
    private final JCheckBox exportServerCheckBox;
    private final Consumer<Boolean> setExportButtonState;

    public CurseforgeExportPanel(Consumer<Boolean> setExportButtonState) {
        super(LEFT_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setExportButtonState = setExportButtonState;

        JLabel formatLabel = new JLabel("<html>Format: <b>" + getExtension() + "</b></html>");
        formatLabel.setBorder(GUIConfiguration.getPaddedBorder(4, 0, 4, 0));
        add(formatLabel);

        JPanel sidesPanel = new JPanel();
        sidesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JLabel sidesLabel = new JLabel("Sides:");
        sidesPanel.add(sidesLabel);

        exportClientCheckBox = new JCheckBox("Client");
        exportClientCheckBox.addActionListener(actionEvent -> updateExportButtonState());
        exportClientCheckBox.setSelected(true);
        sidesPanel.add(exportClientCheckBox);

        exportServerCheckBox = new JCheckBox("Server");
        exportServerCheckBox.addActionListener(actionEvent -> updateExportButtonState());
        exportServerCheckBox.setSelected(false);
        sidesPanel.add(exportServerCheckBox);

        add(sidesPanel);
    }

    private void updateExportButtonState() {
        setExportButtonState.accept(canExport());
    }

    @Override
    public boolean canExport() {
        return exportClientCheckBox.isSelected() || exportServerCheckBox.isSelected();
    }

    @Override
    public List<String> getArguments() {
        List<String> args = new ArrayList<>();
        args.add("curseforge");
        args.add("export");

        if(exportServerCheckBox.isSelected() && exportClientCheckBox.isSelected()) {
            args.add("--side=both");
        } else if(exportClientCheckBox.isSelected()) {
            args.add("--side=client");
        } else {
            args.add("--side=server");
        }
        return args;
    }

    @Override
    public String getExtension() {
        return ".zip";
    }
}

interface ExportPanel {
    boolean canExport();
    String getExtension();
    List<String> getArguments();
}