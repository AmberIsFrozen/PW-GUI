package com.lx862.pwgui.core;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.Util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class ApplicationInfo {
    public String name;
    public String version;
    public String author;
    public String[] news;
    public final OperatingSystem os;

    public static ApplicationInfo INSTANCE;

    public ApplicationInfo() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch");
        OperatingSystem.Type osType = os.contains("windows") ? OperatingSystem.Type.WINDOWS : os.contains("mac") ? OperatingSystem.Type.MAC_OS : os.contains("linux") ? OperatingSystem.Type.LINUX : OperatingSystem.Type.UNKNOWN;
        OperatingSystem.Architecture osArch = arch.equals("aarch64") ? OperatingSystem.Architecture.ARM64 : arch.equals("arm") ? OperatingSystem.Architecture.ARM : OperatingSystem.Architecture.X86;

        this.os = new OperatingSystem(osType, osArch);
    }

    public static void init() {
        try(InputStream is = Util.getAssets("/build.json")) {
            if(is != null) {
                INSTANCE = new Gson().fromJson(new JsonReader(new InputStreamReader(is)), ApplicationInfo.class);
            } else {
                INSTANCE = new ApplicationInfo();
            }
        } catch (Exception e) {
            INSTANCE = new ApplicationInfo();
            PWGUI.LOGGER.error("Failed to read build metadata!", e);
        }
        PWGUI.LOGGER.info("Detected Operating System: {}", INSTANCE.os);
    }

    public record OperatingSystem(Type type, Architecture architecture) {
        public enum Type {
            WINDOWS("Windows"),
            MAC_OS("macOS"),
            LINUX("Linux"),
            UNKNOWN("Unknown");

            private final String name;

            Type(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return this.name;
            }
        }

        public enum Architecture {
            X86("x86"),
            ARM("arm"),
            ARM64("arm64");

            private final String name;

            Architecture(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return this.name;
            }

            public boolean isArm() {
                return this == ARM || this == ARM64;
            }
        }

        @Override
        public String toString() {
            return type.toString() + " " + architecture.toString();
        }
    }
}
