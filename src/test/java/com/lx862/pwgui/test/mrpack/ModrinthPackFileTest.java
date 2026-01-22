package com.lx862.pwgui.test.mrpack;

import com.lx862.pwgui.support.mrpack.ModpackFileEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class ModrinthPackFileTest {
    private ModpackFileEntry file;

    @BeforeEach
    public void prepare() {
        this.file = createMockFile();
    }

    @Test
    public void validateFile() {
        assertDoesNotThrow(() -> file.validate());
    }

    @Test
    public void validateInvalidSha1() {
        file.hashes.sha1 = "0";
        assertThrows(IllegalArgumentException.class, () -> file.validate());
    }

    @Test
    public void validateInvalidSha512() {
        file.hashes.sha512 = "0";
        assertThrows(IllegalArgumentException.class, () -> file.validate());
    }

    @Test
    public void validateBackwardPathTraversal() {
        file.path = "mods/../../lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());

        file.path = "mods\\..\\..\\lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());
    }

    @Test
    public void validateRootPathTraversal() {
        file.path = "/lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());

        file.path = "\\lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());
    }

    @Test
    public void validateDriveLetterPathTraversal() {
        file.path = "C:/lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());

        file.path = "A:\\lithium-neoforge-0.15.1+mc1.21.1.jar";
        assertThrows(IllegalArgumentException.class, () -> file.validate());
    }

    private ModpackFileEntry createMockFile() {
        ModpackFileEntry file = new ModpackFileEntry();
        file.path = "mods/lithium-neoforge-0.15.1+mc1.21.1.jar";
        file.hashes = new ModpackFileEntry.Hashes();
        file.hashes.sha1 = "38295e15a600899697a5dbd29b1e9c15060e5760";
        file.hashes.sha512 = "1975e74294798fe173d944d3e0b174b39dc8b7fa177340acb71c8a85772932b82c9406099c854d45fa4f71b80dbbb73602fd7cc36d1374dfc8402e4a30ccfe3b";
        file.downloads = new URI[]{
            URI.create("https://cdn.modrinth.com/data/gvQqBUqZ/versions/G5SDYehn/lithium-neoforge-0.15.1%2Bmc1.21.1.jar")
        };
        file.env = new ModpackFileEntry.Environment();
        file.env.client = ModpackFileEntry.Environment.SupportType.REQUIRED;
        file.env.server = ModpackFileEntry.Environment.SupportType.REQUIRED;

        return file;
    }
}
