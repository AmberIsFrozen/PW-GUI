package com.lx862.pwgui.gui.panel;

import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.gui.components.kui.KGridBagLayoutPanel;
import com.lx862.pwgui.pwcore.PackFile;
import com.lx862.pwgui.gui.components.kui.KTextField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModpackInfoPanel extends KGridBagLayoutPanel {
    private final KTextField nameTextField;
    private final KTextField authorTextField;
    private final KTextField versionTextField;

    private final String initialName;
    private final String initialAuthor;
    private final String initialVersion;

    public ModpackInfoPanel(PackFile existingFile, Runnable updateSaveState) {
        super(3, 2);
        this.initialName = existingFile == null ? "" : existingFile.name;
        this.initialAuthor = existingFile == null ? Config.getInstance().authorName.getValue() == null ? "" : Config.getInstance().authorName.getValue() : existingFile.author;
        this.initialVersion = existingFile == null ? "" : existingFile.version;

        nameTextField = new KTextField("My Epic Modpack!", true);
        nameTextField.setText(this.initialName);
        nameTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            if(existingFile != null) {
                existingFile.name = nameTextField.getText();
            }
            updateSaveState.run();
        }));
        addRow(1, new JLabel("Name: "), nameTextField);

        authorTextField = new KTextField("Me!", true);
        authorTextField.setText(this.initialAuthor);
        authorTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            if(existingFile != null) {
                existingFile.author = authorTextField.getText();
            }
            updateSaveState.run();
        }));
        addRow(1, new JLabel("Author: "), authorTextField);

        versionTextField = new KTextField("1.0.0", true);
        versionTextField.setText(this.initialVersion);
        versionTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            if(existingFile != null) {
                existingFile.version = versionTextField.getText();
            }
            updateSaveState.run();
        }));
        addRow(1, new JLabel("Version: "), versionTextField);
    }

    public String getModpackName() {
        return nameTextField.getText();
    }

    public boolean shouldSave() {
        return requiredInfoFilled() && (!initialVersion.equals(versionTextField.getText()) || !initialName.equals(nameTextField.getText()) || !initialAuthor.equals(authorTextField.getText()));
    }

    public boolean requiredInfoFilled() {
        return !nameTextField.getText().isEmpty() && !authorTextField.getText().isEmpty() && !versionTextField.getText().isEmpty(); // Packwiz specs does not mandate a modpack version/author, but is required for packwiz CLI when initing
    }

    public List<String> getInitArguments() {
        List<String> st = new ArrayList<>();

        st.add("--name");
        st.add(nameTextField.getText());

        st.add("--author");
        st.add(authorTextField.getText());

        st.add("--version");
        st.add(versionTextField.getText());
        return st;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;
        return size;
    }
}
