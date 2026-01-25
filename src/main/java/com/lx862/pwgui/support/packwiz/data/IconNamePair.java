package com.lx862.pwgui.support.packwiz.data;

import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.util.Util;

import java.awt.*;

public record IconNamePair(String name, Image image) {
    public static final IconNamePair MINECRAFT = new IconNamePair("Minecraft", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/minecraft.png")));
    public static final IconNamePair FABRIC = new IconNamePair("Fabric", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/fabric.png")));
    public static final IconNamePair FORGE = new IconNamePair("Forge", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/forge.png")));
    public static final IconNamePair NEOFORGE = new IconNamePair("NeoForge", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/neoforge.png")));
    public static final IconNamePair QUILT = new IconNamePair("Quilt", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/quiltmc.png")));
    public static final IconNamePair LITELOADER = new IconNamePair("Liteloader", ImageUtil.convertImage(Util.getAssets("/assets/ui/components/liteloader.png")));
    public static final IconNamePair MODRINTH = new IconNamePair("Modrinth", ImageUtil.convertImage(Util.getAssets("/assets/ui/services/modrinth.png")));
    public static final IconNamePair CURSEFORGE = new IconNamePair("CurseForge", ImageUtil.convertImage(Util.getAssets("/assets/ui/services/curseforge.png")));

    @Override
    public String toString() {
        return name;
    }
}