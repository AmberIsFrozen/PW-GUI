package com.lx862.pwgui.gui.panel.editing.filetype;

import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.core.data.model.GitIgnoreRules;

import java.util.function.Consumer;

public class FileEntryPaneContext {
    private final Modpack modpack;
    private final Consumer<GitIgnoreRules> setTreeIgnorePattern;
    private final Consumer<Boolean> shouldSave;
    private final Runnable promptForSaveCallback;

    public FileEntryPaneContext(Modpack modpack, Consumer<GitIgnoreRules> setTreeIgnorePattern, Consumer<Boolean> setSaveCallback, Runnable promptForSaveCallback) {
        this.modpack = modpack;
        this.setTreeIgnorePattern = setTreeIgnorePattern;
        this.shouldSave = setSaveCallback;
        this.promptForSaveCallback = promptForSaveCallback;
    }

    public Modpack getModpack() {
        return this.modpack;
    }

    public void invokeSetTreeIgnorePattern(GitIgnoreRules ignore) {
        setTreeIgnorePattern.accept(ignore);
    }

    public void setShouldSave(boolean bl) {
        shouldSave.accept(bl);
    }

    public void promptForSave() {
        promptForSaveCallback.run();
    }
}
