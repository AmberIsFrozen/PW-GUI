package com.lx862.pwgui.gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.intellijthemes.*;

import javax.swing.*;
import java.util.function.Supplier;

/** Theme that can be chosen within the application */
public enum ApplicationTheme {
    LIGHT("Light", FlatIntelliJLaf::new),
    ARC_DARK("Arc Dark", FlatArcDarkIJTheme::new),
    NORD("Nord", FlatNordIJTheme::new),
    DARK_PURPLE("Dark Purple", FlatDarkPurpleIJTheme::new),
    ONE_DARK("One Dark", FlatOneDarkIJTheme::new),
    HIGH_CONTRAST("High Contrast", FlatHighContrastIJTheme::new);

    private final String displayName;
    private final Supplier<LookAndFeel> lafSupplier;

    ApplicationTheme(String displayName, Supplier<LookAndFeel> lafSupplier) {
        this.displayName = displayName;
        this.lafSupplier = lafSupplier;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public LookAndFeel getLaf() {
        return lafSupplier.get();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
