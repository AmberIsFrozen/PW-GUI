package com.lx862.pwgui.support.packwiz.data;

/* This is what Packwiz defines as Component (at least in the docs IIRC).
* It's just a set of software/modloaders that are applied to your pack.
* I dunno I couldn't explain it any better :P */
public enum PackComponent {
    MINECRAFT(IconNamePair.MINECRAFT, "minecraft", false, VersionGetter::fetchMinecraft),
    FABRIC(IconNamePair.FABRIC, "fabric", true, VersionGetter::fetchFabric),
    FORGE(IconNamePair.FORGE, "forge", true, VersionGetter::fetchForge),
    NEOFORGE(IconNamePair.NEOFORGE, "neoforge", true, VersionGetter::fetchNeoForge),
    QUILT(IconNamePair.QUILT, "quilt", true, VersionGetter::fetchQuilt),
    LITELOADER(IconNamePair.LITELOADER, "liteloader", true, VersionGetter::fetchLiteloader);

    public final IconNamePair iconName;
    public final String slug;
    public final VersionGetter versionGetter;
    public final boolean choosable; // Whether the component can be picked/swapped by the user

    PackComponent(IconNamePair iconName, String slug, boolean choosable, VersionGetter versionGetter) {
        this.iconName = iconName;
        this.slug = slug;
        this.choosable = choosable;
        this.versionGetter = versionGetter;
    }
}