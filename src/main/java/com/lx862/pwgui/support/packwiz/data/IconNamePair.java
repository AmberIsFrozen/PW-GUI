package com.lx862.pwgui.support.packwiz.data;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import java.awt.*;

public enum IconNamePair {
    MINECRAFT("Minecraft", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/minecraft.png"))),
    FABRIC("Fabric", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/fabric.png"))),
    FORGE("Forge", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/forge.png"))),
    NEOFORGE("NeoForge", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/neoforge.png"))),
    QUILT("Quilt", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/quiltmc.png"))),
    LITELOADER("Liteloader", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/liteloader.png"))),
    MODRINTH("Modrinth", ImageUtil.convertImage(Util.getAssets("/assets/ui/services/modrinth.png"))),
    CURSEFORGE("CurseForge", ImageUtil.convertImage(Util.getAssets("/assets/ui/services/curseforge.png")));

    public final String name;
    public final Image image;

    IconNamePair(String name, Image image) {
        this.name = name;
        this.image = image;
    }
}