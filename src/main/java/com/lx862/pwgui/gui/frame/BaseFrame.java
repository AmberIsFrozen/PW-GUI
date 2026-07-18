package com.lx862.pwgui.gui.frame;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.gui.action.*;
import com.lx862.pwgui.gui.dialog.ExportModpackDialog;
import com.lx862.pwgui.gui.dialog.DevServerDialog;
import com.lx862.pwgui.gui.dialog.ViewLogDialog;
import com.lx862.pwgui.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseFrame extends JFrame {
    private static final float[] ZOOM_LEVELS = {1.0f, 1.1f, 1.2f ,1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f, 2.0f};
    protected final JMenuBar jMenuBar;
    private final KeyEventDispatcher shortcutKeyListener;
    private final Map<Float, JCheckBoxMenuItem> zoomDropdownItems = new HashMap<>();
    private int baseWidth;
    private int baseHeight;

    public BaseFrame() {
        this.jMenuBar = new JMenuBar();
        setIconImage(ImageUtil.convertImage(Util.getAssets("/assets/icon.png")));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(jMenuBar);

        UIScale.setSupportedZoomFactors(ZOOM_LEVELS);
        UIScale.setZoomFactor(Config.getInstance().zoomFactor.value());

        shortcutKeyListener = e -> {
            if(e.getID() == KeyEvent.KEY_PRESSED) {
                if(e.isControlDown()) {
                    if(e.getKeyChar() == '+') {
                        if(UIScale.zoomIn()) {
                            updateZoom(UIScale.getZoomFactor());
                            return true;
                        }
                    }
                    if(e.getKeyChar() == '-') {
                        if(UIScale.zoomOut()) {
                            updateZoom(UIScale.getZoomFactor());
                            return true;
                        }
                    }
                }
            }
            return false;
        };
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(shortcutKeyListener);

        UIScale.addPropertyChangeListener((evt) -> {
            if(evt.getPropertyName().equals("zoomFactor")) {
                setSize(this.baseWidth, this.baseHeight);
            }
        });
    }

    public BaseFrame(String title) {
        this();
        setTitle(title);
    }

    @Override
    public void setSize(int width, int height) {
        this.baseWidth = width;
        this.baseHeight = height;
        super.setSize((int)(width * UIScale.getZoomFactor()), (int)(height * UIScale.getZoomFactor()));
    }

    @Override
    public void dispose() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.removeKeyEventDispatcher(shortcutKeyListener);
        super.dispose();
    }

    protected JMenu getHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem clearPWCacheItem = new JMenuItem(new ClearPackwizCacheAction(this));
        helpMenu.add(clearPWCacheItem);

        JMenuItem viewLogMenuItem = new JMenuItem("View Log");
        viewLogMenuItem.setMnemonic(KeyEvent.VK_V);
        viewLogMenuItem.addActionListener(actionEvent -> {
            ViewLogDialog logViewer = new ViewLogDialog(this);
            logViewer.setVisible(true);
        });
        helpMenu.add(viewLogMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem(new AboutAction(this));
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);

        return helpMenu;
    }

    protected JMenu getToolMenu(Modpack modpack) {
        JMenu toolMenu = new JMenu("Tool");

//        JMenuItem refreshMenuItem = new JMenuItem(new RefreshPackAction(this));
//        toolMenu.add(refreshMenuItem);

        JMenuItem generateModlistItem = new JMenuItem(new GenerateModlistAction(this, modpack.packFile.get()));
        toolMenu.add(generateModlistItem);

        JMenuItem devServerMenuItem = new JMenuItem("Run Development Server...");
        devServerMenuItem.setMnemonic(KeyEvent.VK_D);
        toolMenu.add(devServerMenuItem);
        devServerMenuItem.addActionListener(actionEvent -> {
            DevServerDialog frame = new DevServerDialog(this);
            frame.setVisible(true);
        });

        JMenuItem pwConsoleMenuItem = new JMenuItem(new OpenPackwizConsoleAction(this));
        toolMenu.add(pwConsoleMenuItem);

        return toolMenu;
    }

    protected JMenu getEditMenu(Modpack modpack) {
        JMenu editMenu = new JMenu("Edit");
        JMenuItem reinstallMenuItem = new JMenuItem(new ReinstallAction("Reinstall Modpack", this, modpack));
        editMenu.add(reinstallMenuItem);

        JMenuItem updateAllMenuItem = new JMenuItem(new UpdateAction(() -> this));
        editMenu.add(updateAllMenuItem);

        JMenu addMissingMenu = new JMenu("Add Missing...");

        JMenuItem modsDirectoryMenuItem = new JMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "mods", "Mods Folder"));
        JMenuItem configDirectoryMenuItem = new JMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "config", "Config Folder"));
        JMenuItem resourcePacksDirectoryMenuItem = new JMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "resourcepacks", "Resource Packs Folder"));
        JMenuItem shaderPacksDirectoryMenuItem = new JMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "shaderpacks", "Shader Packs Folder"));
        JMenuItem pluginsDirectoryMenuItem = new JMenuItem(new CreateMissingDirectoryAction(this, modpack.getRootPath(), "plugins", "Plugins Folder"));

        addMissingMenu.add(modsDirectoryMenuItem);
        addMissingMenu.add(resourcePacksDirectoryMenuItem);
        addMissingMenu.add(shaderPacksDirectoryMenuItem);
        addMissingMenu.add(configDirectoryMenuItem);
        addMissingMenu.add(pluginsDirectoryMenuItem);

        editMenu.add(addMissingMenu);

        JMenuItem settingsItem = new JMenuItem(new SettingsAction(this));
        editMenu.add(settingsItem);

        return editMenu;
    }

    protected JMenu getFileMenu(Modpack modpack, Consumer<Boolean> saveAllCallback) {
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveMenuItem = new JMenuItem("Save Selected File");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(actionEvent -> saveAllCallback.accept(false));
        fileMenu.add(saveMenuItem);

//        JMenuItem importMenuItem = new JMenuItem("Import Pack...");
//        importMenuItem.setMnemonic(KeyEvent.VK_I);
//        importMenuItem.addActionListener(actionEvent -> {
//            saveAllCallback.accept(false);
//            new ImportModpackDialog(this).setVisible(true);
//        });
//        fileMenu.add(importMenuItem);

        JMenuItem exportMenuItem = new JMenuItem("Export Pack...");
        exportMenuItem.setMnemonic(KeyEvent.VK_E);
        exportMenuItem.addActionListener(actionEvent -> {
            saveAllCallback.accept(false);
            new ExportModpackDialog(this, modpack).setVisible(true);
        });
        fileMenu.add(exportMenuItem);

        JMenuItem quitMenuItem = new JMenuItem("Quit...");
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);

        quitMenuItem.addActionListener(actionEvent -> {
            WelcomeFrame welcomeFrame = new WelcomeFrame(this);
            dispose();
            welcomeFrame.setVisible(true);
        });
        fileMenu.add(quitMenuItem);

        return fileMenu;
    }

    protected JMenu getViewMenu() {
        JMenu viewMenu = new JMenu("View");
        JMenu zoomMenu = new JMenu("Zoom...");

        for(float zoomFactor : ZOOM_LEVELS) {
            int scalePercentage = (int)(zoomFactor * 100);
            JCheckBoxMenuItem zoomLevelMenu = new JCheckBoxMenuItem(scalePercentage + "%");
            zoomLevelMenu.addActionListener(actionEvent -> {
                if(UIScale.setZoomFactor(zoomFactor)) {
                    updateZoom(zoomFactor);
                }
            });

            zoomDropdownItems.put(zoomFactor, zoomLevelMenu);
            zoomLevelMenu.setSelected(UIScale.getZoomFactor() == zoomFactor);
            zoomMenu.add(zoomLevelMenu);
        }

        viewMenu.add(zoomMenu);
        return viewMenu;
    }

    private void updateZoom(float zoomFactor) {
        Config.getInstance().zoomFactor.setValue(zoomFactor);
        try {
            Config.getInstance().write("Update zoom level");
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
        }

        FlatLaf.updateUI();
        zoomDropdownItems.forEach((itemZoomFactor, zoomLevelMenu) -> {
            zoomLevelMenu.setSelected(UIScale.getZoomFactor() == itemZoomFactor);
        });
    }
}
