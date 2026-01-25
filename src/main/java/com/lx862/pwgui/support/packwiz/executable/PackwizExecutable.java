package com.lx862.pwgui.support.packwiz.executable;

import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.executable.Executable;
import com.lx862.pwgui.support.packwiz.data.PackComponentVersion;

import java.nio.file.Path;

public class PackwizExecutable extends Executable {
    public static final Logger LOGGER = new Logger("PackwizExec");
    public static final PackwizExecutable INSTANCE = new PackwizExecutable(LOGGER);

    private final Url url;
    private final Settings settings;
    private final CurseForge curseForge;
    private final Modrinth modrinth;
    private String packFileLocation = null;

    public PackwizExecutable(Logger logger) {
        super(logger, "Packwiz");
        keywords.add("A command line tool for creating Minecraft modpacks");
        keywords.add("Use \"packwiz [command] --help\" for more information about a command.");

        potentialPaths.add("packwiz"); // Added in PATH
        potentialPaths.add("/etc/profiles/per-user/" + System.getProperty("user.name") + "/bin/packwiz"); // NixOS

        this.url = new Url();
        this.settings = new Settings();
        this.curseForge = new CurseForge();
        this.modrinth = new Modrinth();
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
        Path configuredPackwizExecutablePath = Config.getInstance().packwizExecutablePath.value();
        if(configuredPackwizExecutablePath != null) {
            if(isOurIntendedProgram(configuredPackwizExecutablePath.toString())) {
                LOGGER.info("Found executable at {}", configuredPackwizExecutablePath);
                return configuredPackwizExecutablePath.toString();
            }
        }
        return super.probe(override);
    }

    public void setPackFileLocation(String str) {
        this.packFileLocation = str;
    }

    /* Commands */

    public PackwizArgumentBuilder init(String name, String author, String version, String minecraftVersion, PackComponentVersion modloader) {
        PackwizArgumentBuilder commandBuilder = buildCommand("init", "--name", name, "--author", author, "--version", version, "--mc-version", minecraftVersion, "--modloader", modloader == null ? "none" : modloader.getComponent().slug);
        if(modloader != null) {
            commandBuilder.append("--" + modloader.getComponent().slug + "-version", modloader.getVersion());
        }
        return commandBuilder;
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
        public PackwizArgumentBuilder add(String name, String url, boolean force) {
            PackwizArgumentBuilder argumentBuilder = buildCommand("url", "add", name, url);
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

    public Modrinth modrinth() {
        return modrinth;
    }

    public class Modrinth {
        public PackwizArgumentBuilder add(String name) {
            return buildCommand("modrinth", "add", name);
        }
    }

    public class PackwizArgumentBuilder extends ProgramArgumentBuilder {
        public PackwizArgumentBuilder(String... args) {
            super(args);
        }

        public PackwizArgumentBuilder metaFolder(String str) {
            if(str != null) append("--meta-folder", str);
            return this;
        }

        public PackwizArgumentBuilder packFile(String packFileLocation) {
            if(packFileLocation != null) append("--pack-file", packFileLocation);
            return this;
        }

        public PackwizArgumentBuilder yes() {
            append("--yes");
            return this;
        }
    }
}
