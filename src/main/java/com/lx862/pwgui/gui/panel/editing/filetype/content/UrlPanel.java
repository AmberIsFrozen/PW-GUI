package com.lx862.pwgui.gui.panel.editing.filetype.content;

import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.components.kui.KButton;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.core.data.model.file.ContentDirectoryModel;
import com.lx862.pwgui.executable.ProgramExecution;
import com.lx862.pwgui.gui.components.kui.KGridBagLayoutPanel;
import com.lx862.pwgui.gui.components.kui.KTextField;
import com.lx862.pwgui.gui.prompt.TaskProgressDialog;
import com.lx862.pwgui.gui.panel.editing.filetype.FileEntryPaneContext;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class UrlPanel extends JPanel {
    private static final HashMap<String, String> alternativeForDomain = new HashMap<>();

    static {
        alternativeForDomain.put("modrinth.com", "Modrinth");
        //alternativeForDomain.put("github.com", "GitHub"); // Doesn't seem finished?
        alternativeForDomain.put("curseforge.com", "Curseforge");
        alternativeForDomain.put("forgecdn.net", "Curseforge");
    }

    public UrlPanel(FileEntryPaneContext context, ContentDirectoryModel fileEntry) {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        KGridBagLayoutPanel formPanel = new KGridBagLayoutPanel(3, 2);
        formPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel("You can add any arbitrary file to packwiz based on a direct URL link.");
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        formPanel.addRow(2, descriptionLabel);

        KTextField nameTextField = new KTextField("testmod");
        formPanel.addRow(1, new JLabel("Name: "), nameTextField);

        KTextField urlTextField = new KTextField("https://example.com");
        urlTextField.addActionListener(actionEvent -> {
            if(!nameTextField.getText().isEmpty() && !urlTextField.getText().isEmpty()) {
                addProject(context, fileEntry, nameTextField.getText(), urlTextField.getText());
            }
        });
        formPanel.addRow(1, new JLabel("URL: "), urlTextField);

        JLabel invalidUrlLabel = new JLabel("Please enter a valid URL!");
        invalidUrlLabel.setForeground(Color.RED);
        formPanel.addRow(2, invalidUrlLabel);

        KButton addButton = new KButton("Add Item");
        addButton.setMnemonic(KeyEvent.VK_A);
        addButton.setAlignmentX(LEFT_ALIGNMENT);
        formPanel.addRow(2, addButton);

        nameTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> updateInstallButtonState(addButton, nameTextField, urlTextField, invalidUrlLabel)));
        urlTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> updateInstallButtonState(addButton, nameTextField, urlTextField, invalidUrlLabel)));

        updateInstallButtonState(addButton, nameTextField, urlTextField, invalidUrlLabel);

        addButton.addActionListener(actionEvent -> addProject(context, fileEntry, nameTextField.getText(), urlTextField.getText()));

        mainPanel.add(formPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addProject(FileEntryPaneContext context, ContentDirectoryModel fileEntry, String name, String urlString) {
        try {
            URI url = new URI(urlString);
            if(alternativeForDomain.containsKey(url.getHost())) {
                String alternativeName = alternativeForDomain.get(url.getHost());
                if(JOptionPane.showConfirmDialog(getTopLevelAncestor(), String.format("Adding content from %s is supported natively in this application.\nBy continuing, you won't get auto-update and other %s-specific features.\nContinue anyway?", alternativeName, alternativeName), Util.withTitlePrefix("Use Alternate Installation Method?"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        } catch (URISyntaxException ignored) {
        }

        ProgramExecution programExecution = PackwizExecutable.INSTANCE.url().add(name, urlString, true).metaFolder(context.getModpack().getRootPath().relativize(fileEntry.path).toString()).build(); // We already did a domain check before, so forcibly add it anyway.
        programExecution.onExit((exitCode) -> {
            if(exitCode == 0) {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), String.format("%s has been added!", name), Util.withTitlePrefix("Item Added!"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        new TaskProgressDialog((Window)getTopLevelAncestor(), "Adding item...", "Triggered by user", programExecution).setVisible(true);
    }

    private void updateInstallButtonState(KButton addButton, KTextField nameTextField, KTextField urlTextField, JLabel urlInvalidLabel) {
        boolean shouldEnable = true;
        urlInvalidLabel.setVisible(false);
        if(nameTextField.getText().isEmpty() || urlTextField.getText().isEmpty()) {
            shouldEnable = false;
        } else {
            try {
                new URI(urlTextField.getText());
            } catch (URISyntaxException e) {
                urlInvalidLabel.setVisible(true);
                shouldEnable = false;
            }
        }

        addButton.setEnabled(shouldEnable);
    }
}
