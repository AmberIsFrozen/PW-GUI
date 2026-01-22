package com.lx862.pwgui;

import org.apache.commons.cli.*;

/**
 * Entrypoint for the program
 */
public class Main {
    private static final Options cliOptions = new Options();

    static {
        cliOptions.addOption("e", "pwexec", true, "Specify the path to packwiz executable");
        cliOptions.addOption("p", "pack", true, "Specify the location of pack.toml, automatically opening the Modpack.");
    }

    public static void main(String[] args) {
        CommandLineParser cliParser = new DefaultParser();

        try {
            CommandLine cmd = cliParser.parse(cliOptions, args);
            PWGUI.start(cmd);
        } catch (Exception e) {
            PWGUI.LOGGER.error("Failed to start program!", e);
            System.exit(1);
        }
    }
}