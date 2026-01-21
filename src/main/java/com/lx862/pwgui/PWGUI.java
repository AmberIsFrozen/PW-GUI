package com.lx862.pwgui;

import com.formdev.flatlaf.util.UIScale;
import com.lx862.pwgui.core.BuildMetadata;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.core.Logger;
import com.lx862.pwgui.executable.Executables;
import com.lx862.pwgui.gui.frame.EditFrame;
import com.lx862.pwgui.gui.frame.SetupFrame;
import com.lx862.pwgui.gui.frame.WelcomeFrame;
import com.lx862.pwgui.pwcore.Modpack;
import com.lx862.pwgui.util.GUIHelper;
import org.apache.commons.cli.CommandLine;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class PWGUI {
    public static final Logger LOGGER = new Logger();
    private static final Config config = new Config();

    /**
     * Initialize/re-initialize the program
     * @param commandLine The CommandLine parsed from the CLI. Null if this is a reinitialization process.
     */
    public static void init(CommandLine commandLine) {
        BuildMetadata.init();

        try {
            config.read();
        } catch (Exception e) {
            if(e instanceof FileNotFoundException) {
                LOGGER.info("Config file does not exist.");
            } else {
                LOGGER.exception(e);
                LOGGER.warn("Failed to read config file, using default!");
            }
        }

        String packFilePath = config.openLastModpackOnLaunch.getValue() ? config.lastModpackPath.getValue() == null ? null : config.lastModpackPath.getValue().toString() : null;
        boolean packwizLocated;

        if(commandLine != null) {
            String packwizPathOverride = commandLine.getOptionValue("pwexec");
            String packFilePathOverride = commandLine.getOptionValue("pack");
            packwizLocated = Executables.packwiz.updateExecutableLocation(packwizPathOverride);
            if(packFilePathOverride != null) packFilePath = packFilePathOverride;
        } else {
            packwizLocated = Executables.packwiz.updateExecutableLocation(null);
        }
        // final boolean gitLocated = git.updateExecutableLocation(null); // We don't have git support yet
        launchGUI(packFilePath, packwizLocated);
    }

    private static void launchGUI(String packFilePath, boolean packwizLocated) {
        Config config = getConfig();
        GUIHelper.setupApplicationTheme(config.applicationTheme.getValue(), config.useWindowDecoration.getValue(), null); // Initialize FlatLaf and it's config
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
        LOGGER.info(String.format("Pack File is specified at: %s", path));
        try {
            return new Modpack(packFile.toPath());
        } catch (FileNotFoundException e) {
            LOGGER.info("Specified Pack File does not exist!");
            return null;
        }
    }

    public static Config getConfig() {
        return config;
    }
}