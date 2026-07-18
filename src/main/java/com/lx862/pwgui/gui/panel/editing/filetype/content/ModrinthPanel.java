package com.lx862.pwgui.gui.panel.editing.filetype.content;

import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.core.data.model.file.ContentDirectoryModel;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.components.kui.KGridBagLayoutPanel;
import com.lx862.pwgui.gui.components.kui.KHelpButton;
import com.lx862.pwgui.gui.components.kui.KTextField;
import com.lx862.pwgui.gui.panel.editing.filetype.FileEntryPaneContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ModrinthPanel extends JPanel {
    public ModrinthPanel(FileEntryPaneContext context, ContentDirectoryModel fileEntry) {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        KGridBagLayoutPanel formPanel = new KGridBagLayoutPanel(3, 2);
        formPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel("You can add file to packwiz with projects from Modrinth");
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        formPanel.addRow(2, descriptionLabel);

        KTextField contentTextField = new KTextField("Mod name here...");
        contentTextField.addActionListener(actionEvent -> {
            if(!contentTextField.getText().isEmpty()) AddContentPanel.addProjectFromContentPlatform((Window)getTopLevelAncestor(), context.getModpack(), "modrinth", "add", contentTextField.getText());
        });
        formPanel.addRow(1, new JLabel("URL/Search Term: "), contentTextField);

        JPanel forceCurrentFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JCheckBox forceCurrentFolderCheckBox = new JCheckBox("Always install to current folder instead of auto detection");
        forceCurrentFolderPanel.add(forceCurrentFolderCheckBox);
        forceCurrentFolderPanel.add(new KHelpButton("When installing contents from Modrinth/CurseForge, packwiz will automatically determine the folder to use based on the project type.\nBy selecting the checkbox, you can force packwiz to always install to the current folder."));
        formPanel.addRow(2, forceCurrentFolderPanel);

        KButton addButton = new KButton("Add Project");
        addButton.addActionListener(actionEvent -> {
            String[] argsToUse;
            if(forceCurrentFolderCheckBox.isSelected()) {
                argsToUse = new String[]{"modrinth", "add", contentTextField.getText(), "--meta-folder", context.getModpack().getRootPath().relativize(fileEntry.path).toString()};
            } else {
                argsToUse = new String[]{"modrinth", "add", contentTextField.getText()};
            }
            AddContentPanel.addProjectFromContentPlatform((Window)getTopLevelAncestor(), context.getModpack(), argsToUse);
        });
        addButton.setMnemonic(KeyEvent.VK_A);
        addButton.setAlignmentX(LEFT_ALIGNMENT);
        formPanel.addRow(2, addButton);

        contentTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> updateAddProjectButtonState(addButton, contentTextField)));

        updateAddProjectButtonState(addButton, contentTextField);

        mainPanel.add(formPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void updateAddProjectButtonState(KButton addButton, KTextField urlTextField) {
        boolean shouldEnable = !urlTextField.getText().isEmpty();
        addButton.setEnabled(shouldEnable);
    }
}
