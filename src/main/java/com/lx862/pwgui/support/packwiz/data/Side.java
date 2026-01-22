package com.lx862.pwgui.support.packwiz.data;

/* Represent a "side" in Minecraft, either Client or Server side (or both) */
public enum Side {
    client("client"),
    server("server"),
    both("both");

    private final String str;

    Side(String str) {
        this.str = str;
    }

    public boolean isClientSupported() {
        return this == Side.both || this == Side.client;
    }

    public boolean isServerSupported() {
        return this == Side.both || this == Side.server;
    }

    @Override
    public String toString() {
        return str;
    }
}
