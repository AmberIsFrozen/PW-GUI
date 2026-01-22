package com.lx862.pwgui;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.core.ApplicationInfo;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.support.packwiz.executable.PackwizExecutable;
import com.lx862.pwgui.gui.frame.EditFrame;
import com.lx862.pwgui.gui.frame.SetupFrame;
import com.lx862.pwgui.gui.frame.WelcomeFrame;
import com.lx862.pwgui.support.packwiz.Modpack;
import com.lx862.pwgui.gui.GUIConfiguration;
import org.apache.commons.cli.CommandLine;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class PWGUI {
    public static final Logger LOGGER = new Logger("PW-GUI");

    /**
     * Initialize/re-initialize the program
     * @param commandLine The CommandLine parsed from the CLI. Null if this is a reinitialization process.
     */
    public static void start(CommandLine commandLine) {
        ApplicationInfo.init();
        init(commandLine);
    }

    public static void init(CommandLine commandLine) {
        try {
            Config.init();
        } catch (Exception e) {
            LOGGER.error("Failed to read config file!", e);
        }

        String packFilePath = Config.getInstance().openLastModpackOnLaunch.getValue() ? Config.getInstance().lastModpackPath.getValue() == null ? null : Config.getInstance().lastModpackPath.getValue().toString() : null;
        boolean packwizLocated;

        if(commandLine != null) {
            String packwizPathOverride = commandLine.getOptionValue("pwexec");
            String packFilePathOverride = commandLine.getOptionValue("pack");
            packwizLocated = PackwizExecutable.INSTANCE.updateExecutableLocation(packwizPathOverride);
            if(packFilePathOverride != null) packFilePath = packFilePathOverride;
        } else {
            packwizLocated = PackwizExecutable.INSTANCE.updateExecutableLocation(null);
        }

        // final boolean gitLocated = git.updateExecutableLocation(null); // We don't have git support yet
        launchGUI(packFilePath, packwizLocated);
    }

    private static void launchGUI(String packFilePath, boolean packwizLocated) {
        Config config = Config.getInstance();
        GUIConfiguration.setupGUI(config.applicationTheme.getValue(), config.useWindowDecoration.getValue(), null); // Initialize FlatLaf and it's config
        UIScale.setZoomFactor(config.zoomFactor.getValue());

        if(!packwizLocated) { // No packwiz, show setup wizard
            SwingUtilities.invokeLater(() -> {
                SetupFrame setupFrame = new SetupFrame(null);
                setupFrame.setVisible(true);
            });
            return;
        }

        Modpack modpack = openModpack(packFilePath);
        if(modpack != null) { // Pack specified via CLI or last opened
            SwingUtilities.invokeLater(() -> {
                EditFrame editFrame = new EditFrame(null, modpack);
                editFrame.setVisible(true);
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                WelcomeFrame welcomeFrame = new WelcomeFrame(null);
                welcomeFrame.setVisible(true);
            });
        }
    }

    private static Modpack openModpack(String path) {
        if(path == null) return null;

        File packFile = new File(path);
        LOGGER.info("Pack File is specified at: {}", path);
        try {
            return new Modpack(packFile.toPath());
        } catch (FileNotFoundException e) {
            LOGGER.info("Specified Pack File does not exist!");
            return null;
        }
    }
}