package com.lx862.pwgui.support.packwiz;

import com.lx862.pwgui.core.data.Cache;
import com.lx862.pwgui.core.data.model.GitIgnoreRules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Modpack {
    public static final GitIgnoreRules defaultFileIgnoreRules = new GitIgnoreRules(
        // Sourced from https://github.com/packwiz/packwiz/blob/0626c00149a8d9a5e9f76e5640e7b8b95c064350/core/index.go#L131-L151
        new String[] {
            ".git/**",
            ".gitattributes",
            ".gitignore",

            // Exclude macOS metadata
            ".DS_Store",

            // Exclude exported CurseForge zip files
            "/*.zip",

            // Exclude exported Modrinth packs
            "*.mrpack",

            // Exclude packwiz binaries, if the user puts them in their pack folder
            "packwiz.exe",
            "packwiz"
        }
    );

    private final Path root;
    private final Path packFilePath;
    public final Cache<PackFile> packFile;

    public Modpack(Path packFilePath) throws FileNotFoundException {
        this.packFilePath = packFilePath;
        this.root = packFilePath.getParent();
        if(Files.isDirectory(packFilePath) || Files.notExists(packFilePath)) throw new FileNotFoundException(String.format("Cannot find target file: %s. Expected a toml file containing modpack config!", packFilePath));
        this.packFile = new Cache<>(() -> {
            try {
                return new PackFile(packFilePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        this.packFile.get();
    }

    public Path getPackFilePath() {
        return this.packFilePath;
    }

    public Path getRootPath() {
        return this.root;
    }

    /** Returns whether the specified path is a mandatory file required for the pack to function. */
    public boolean isCriticalFile(Path path) {
        return getPackFilePath().equals(path) || this.packFile.get().getIndexPath().equals(path);
    }

    public GitIgnoreRules getPackwizIgnore() {
        Path packwizIgnoreFile = this.root.resolve(".packwizignore");
        GitIgnoreRules bonusRules;

        if(Files.exists(packwizIgnoreFile)) {
            try {
                bonusRules = new GitIgnoreRules(String.join("\n", Files.readAllLines(packwizIgnoreFile)));
            } catch (IOException e) {
                bonusRules = null;
            }
        } else {
            bonusRules = null;
        }

        return bonusRules == null ? defaultFileIgnoreRules : defaultFileIgnoreRules.overlay(bonusRules);
    }
}
