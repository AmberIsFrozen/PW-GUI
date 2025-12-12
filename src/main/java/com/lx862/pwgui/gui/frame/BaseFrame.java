package com.lx862.pwgui.gui.frame;

import com.lx862.pwgui.pwcore.Modpack;
import com.lx862.pwgui.gui.action.*;
import com.lx862.pwgui.gui.components.kui.KMenu;
import com.lx862.pwgui.gui.components.kui.KMenuItem;
import com.lx862.pwgui.gui.dialog.ExportModpackDialog;
import com.lx862.pwgui.gui.dialog.DevServerDialog;
import com.lx862.pwgui.gui.dialog.ImportModpackDialog;
import com.lx862.pwgui.gui.dialog.ViewLogDialog;
import com.lx862.pwgui.util.GUIHelper;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public abstract class BaseFrame extends JFrame {
    protected final JMenuBar jMenuBar;

    public BaseFrame() {
        this.jMenuBar = new JMenuBar();
        setIconImage(GUIHelper.convertImage(Util.getAssets("/assets/icon.png")));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(jMenuBar);
    }

    public BaseFrame(String title) {
        this();
        setTitle(title);
    }

    protected KMenu getHelpMenu() {
        KMenu helpMenu = new KMenu("Help");

        KMenuItem clearPWCacheItem = new KMenuItem(new ClearPackwizCacheAction(this));
        helpMenu.add(clearPWCacheItem);

        KMenuItem viewLogMenuItem = new KMenuItem("View Log");
        viewLogMenuItem.setMnemonic(KeyEvent.VK_V);
        viewLogMenuItem.addActionListener(actionEvent -> {
            ViewLogDialog logViewer = new ViewLogDialog(this);
            logViewer.setVisible(true);
        });
        helpMenu.add(viewLogMenuItem);

        KMenuItem aboutMenuItem = new KMenuItem(new AboutAction(this));
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);

        return helpMenu;
    }

    protected KMenu getToolMenu(Modpack modpack) {
        KMenu toolMenu = new KMenu("Tool");

//        KMenuItem refreshMenuItem = new KMenuItem(new RefreshPackAction(this));
//        toolMenu.add(refreshMenuItem);

        KMenuItem generateModlistItem = new KMenuItem(new GenerateModlistAction(this, modpack.packFile.get()));
        toolMenu.add(generateModlistItem);

        KMenuItem devServerMenuItem = new KMenuItem("Run Development Server...");
        devServerMenuItem.setMnemonic(KeyEvent.VK_D);
        toolMenu.add(devServerMenuItem);
        devServerMenuItem.addActionListener(actionEvent -> {
            DevServerDialog frame = new DevServerDialog(this);
            frame.setVisible(true);
        });

        KMenuItem pwConsoleMenuItem = new KMenuItem(new OpenPackwizConsoleAction(this));
        toolMenu.add(pwConsoleMenuItem);

        return toolMenu;
    }

    protected KMenu getEditMenu(Modpack modpack) {
        KMenu editMenu = new KMenu("Edit");
        KMenuItem reinstallMenuItem = new KMenuItem(new ReinstallAction("Reinstall Modpack", this, modpack));
        editMenu.add(reinstallMenuItem);

        KMenuItem updateAllMenuItem = new KMenuItem(new UpdateAction(() -> this));
        editMenu.add(updateAllMenuItem);

        KMenu addMissingMenu = new KMenu("Add Missing...");

        KMenuItem modsDirectoryMenuItem = new KMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "mods", "Mods Folder"));
        KMenuItem configDirectoryMenuItem = new KMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "config", "Mod Config Folder"));
        KMenuItem resourcePacksDirectoryMenuItem = new KMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "resourcepacks", "Resource Packs Folder"));
        KMenuItem shaderPacksDirectoryMenuItem = new KMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "shaderpacks", "Shader Packs Folder"));
        KMenuItem pluginsDirectoryMenuItem = new KMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "plugins", "Plugins Folder"));

        addMissingMenu.add(modsDirectoryMenuItem);
        addMissingMenu.add(resourcePacksDirectoryMenuItem);
        addMissingMenu.add(shaderPacksDirectoryMenuItem);
        addMissingMenu.add(configDirectoryMenuItem);
        addMissingMenu.add(pluginsDirectoryMenuItem);

        editMenu.add(addMissingMenu);

        KMenuItem settingsItem = new KMenuItem(new SettingsAction(this));
        editMenu.add(settingsItem);

        return editMenu;
    }

    protected KMenu getFileMenu(Modpack modpack, Consumer<Boolean> saveAllCallback) {
        KMenu fileMenu = new KMenu("File");

        KMenuItem saveMenuItem = new KMenuItem("Save Selected File");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(actionEvent -> saveAllCallback.accept(false));
        fileMenu.add(saveMenuItem);

        KMenuItem importMenuItem = new KMenuItem("Import Pack...");
        importMenuItem.setMnemonic(KeyEvent.VK_I);
        importMenuItem.addActionListener(actionEvent -> {
            saveAllCallback.accept(false);
            new ImportModpackDialog(this).setVisible(true);
        });
        fileMenu.add(importMenuItem);

        KMenuItem exportMenuItem = new KMenuItem("Export Pack...");
        exportMenuItem.setMnemonic(KeyEvent.VK_E);
        exportMenuItem.addActionListener(actionEvent -> {
            saveAllCallback.accept(false);
            new ExportModpackDialog(this, modpack).setVisible(true);
        });
        fileMenu.add(exportMenuItem);

        KMenuItem quitMenuItem = new KMenuItem("Quit...");
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);

        quitMenuItem.addActionListener(actionEvent -> {
            WelcomeFrame welcomeFrame = new WelcomeFrame(this);
            dispose();
            welcomeFrame.setVisible(true);
        });
        fileMenu.add(quitMenuItem);

        return fileMenu;
    }
}
