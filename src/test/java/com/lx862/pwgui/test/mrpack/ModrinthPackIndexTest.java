package com.lx862.pwgui.test.mrpack;

import com.lx862.pwgui.support.mrpack.ModpackIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ModrinthPackIndexTest {
    private ModpackIndex file;

    @BeforeEach
    public void prepare() {
        file = createMockIndex();
    }

    @Test
    public void validateIndex() {
        assertDoesNotThrow(() -> file.validateSpec());
    }

    private ModpackIndex createMockIndex() {
        ModpackIndex modpackIndex = new ModpackIndex();
        modpackIndex.formatVersion = 1;
        modpackIndex.versionId = "2.1.1";
        modpackIndex.name = "Test Modpack";
        modpackIndex.game = "minecraft";
        modpackIndex.summary = "Test Description";
        modpackIndex.dependencies = new HashMap<>();
        modpackIndex.dependencies.put(ModpackIndex.DEP_MINECRAFT, "1.21.1");
        modpackIndex.dependencies.put(ModpackIndex.DEP_FABRIC, "0.16.9");
        modpackIndex.files = new ArrayList<>();
        return modpackIndex;
    }
}
