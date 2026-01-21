package com.lx862.pwgui.executable;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;

import java.nio.file.Path;

public class PackwizExecutable extends Executable {
    private String packFileLocation = null;
    private final Url url;
    private final Settings settings;
    private final CurseForge curseForge;

    public PackwizExecutable() {
        super("Packwiz");
        keywords.add("A command line tool for creating Minecraft modpacks");
        keywords.add("Use \"packwiz [command] --help\" for more information about a command.");

        potentialPaths.add("packwiz"); // Added in PATH
        potentialPaths.add("/etc/profiles/per-user/" + System.getProperty("user.name") + "/bin/packwiz"); // NixOS

        this.url = new Url();
        this.settings = new Settings();
        this.curseForge = new CurseForge();
    }

    @Override
    public PackwizArgumentBuilder buildCommand(String... str) {
        PackwizArgumentBuilder argumentBuilder = new PackwizArgumentBuilder(str);
        if(packFileLocation != null) {
            argumentBuilder.packFile(packFileLocation);
        }
        return argumentBuilder;
    }

    @Override
    public String probe(String override) {
        Path configuredPackwizExecutablePath = Config.getInstance().packwizExecutablePath.getValue();
        if(configuredPackwizExecutablePath != null) {
            if(isOurIntendedProgram(configuredPackwizExecutablePath.toString())) {
                PWGUI.LOGGER.info("{} executable configured at {}", programName, configuredPackwizExecutablePath);
                return configuredPackwizExecutablePath.toString();
            }
        }
        return super.probe(override);
    }

    public void setPackFileLocation(String str) {
        this.packFileLocation = str;
    }

    /* Commands */

    public PackwizArgumentBuilder init() {
        return buildCommand("init");
    }


    public PackwizArgumentBuilder refresh() {
        return buildCommand("refresh");
    }

    public PackwizArgumentBuilder update(String slug) {
        return buildCommand("update", slug);
    }

    public PackwizArgumentBuilder updateAll() {
        return buildCommand("update", "--all");
    }

    public PackwizArgumentBuilder remove(String slug) {
        return buildCommand("remove", slug);
    }

    public PackwizArgumentBuilder serve() {
        return buildCommand("serve");
    }


    public Url url() {
        return url;
    }

    public class Url {
        public PackwizArgumentBuilder add(String name, String url, String metaFolder, boolean force) {
            PackwizArgumentBuilder argumentBuilder = buildCommand("url", "add", name, url).metaFolder(metaFolder);
            if(force) {
                argumentBuilder.append("--force");
            }
            return argumentBuilder;
        }
    }

    public Settings settings() {
        return settings;
    }

    public class Settings {
        public PackwizArgumentBuilder addAcceptableVersions(String version) {
            return buildCommand("settings", "acceptable-versions", "--add", version);
        }

        public PackwizArgumentBuilder removeAcceptableVersions(String version) {
            return buildCommand("settings", "acceptable-versions", "--remove", version);
        }
    }

    public CurseForge curseForge() {
        return curseForge;
    }

    public class CurseForge {
        public PackwizArgumentBuilder importPack(String modpackPath) {
            return buildCommand("curseforge", "import", modpackPath);
        }
    }

    public class PackwizArgumentBuilder extends ProgramArgumentBuilder {
        public PackwizArgumentBuilder(String... args) {
            super(args);
        }

        public PackwizArgumentBuilder metaFolder(String str) {
            append("--meta-folder", str);
            return this;
        }

        public PackwizArgumentBuilder packFile(String packFileLocation) {
            append("--pack-file", packFileLocation);
            return this;
        }

        public PackwizArgumentBuilder yes() {
            append("--yes");
            return this;
        }
    }
}
