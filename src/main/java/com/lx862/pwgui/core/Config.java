package com.lx862.pwgui.core;

import com.google.gson.*;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.data.ApplicationTheme;
import com.lx862.pwgui.support.packwiz.WritableFile;
import com.lx862.pwgui.util.GoUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Config for the main program */
public class Config extends WritableFile {
    public static final Path CONFIG_DIR_PATH = GoUtil.getUserConfigDir().resolve("pwgui");
    private static final Path CONFIG_PATH = CONFIG_DIR_PATH.resolve("config.json");
    private static Config INSTANCE;

    public final Map<String, Path> fileChooserLastPath = new HashMap<>();

    public final ConfigEntry<ApplicationTheme> applicationTheme = new ConfigEntry<>("applicationTheme", ApplicationTheme.LIGHT, (jsonElement -> ApplicationTheme.valueOf(jsonElement.getAsString())));
    public final ConfigEntry<String> authorName = new ConfigEntry<>("authorName", null, (jsonElement -> jsonElement.getAsString()));
    public final ConfigEntry<Boolean> debugMode = new ConfigEntry<>("debugMode", false, (jsonElement -> jsonElement.getAsBoolean()));
    public final ConfigEntry<Float> zoomFactor = new ConfigEntry<>("zoomFactor", 1.0f, (jsonElement -> jsonElement.getAsFloat()));
    public final ConfigEntry<Boolean> openLastModpackOnLaunch = new ConfigEntry<>("openLastModpackOnLaunch", true, (jsonElement -> jsonElement.getAsBoolean()));
    public final ConfigEntry<Boolean> useWindowDecoration = new ConfigEntry<>("useWindowDecoration", false, (jsonElement -> jsonElement.getAsBoolean()));
    public final ConfigEntry<Boolean> showMetaFileName = new ConfigEntry<>("showMetaFileName", false, (jsonElement -> jsonElement.getAsBoolean()));
    public final ConfigEntry<Path> lastModpackPath = new ConfigEntry<>("lastModpackPath", null, (jsonElement -> Paths.get(jsonElement.getAsString())));
    public final ConfigEntry<Path> packwizExecutablePath = new ConfigEntry<>("packwiz", null, (jsonElement -> Paths.get(jsonElement.getAsString())));

    public Config() {
        super(CONFIG_PATH);
    }

    public static void init() throws IOException {
        INSTANCE = new Config();
        INSTANCE.read();
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public void read() throws IOException {
        if(Files.exists(CONFIG_PATH)) {
            JsonObject configJson = JsonParser.parseString(FileUtils.readFileToString(CONFIG_PATH.toFile(), StandardCharsets.UTF_8)).getAsJsonObject();
            if(configJson.has("executables")) {
                JsonObject executableObject = configJson.getAsJsonObject("executables");
                this.packwizExecutablePath.read(executableObject);
            }

            if(configJson.has("lastPickedFiles")) {
                JsonArray lastPickedFiles = configJson.getAsJsonArray("lastPickedFiles");

                for(int i = 0; i < lastPickedFiles.size(); i++) {
                    JsonObject entry = lastPickedFiles.get(i).getAsJsonObject();
                    String contextName = entry.get("context").getAsString();
                    String path = entry.get("path").getAsString();
                    fileChooserLastPath.put(contextName, Paths.get(path));
                }
            }

            this.applicationTheme.read(configJson);
            this.authorName.read(configJson);
            this.debugMode.read(configJson);
            this.openLastModpackOnLaunch.read(configJson);
            this.useWindowDecoration.read(configJson);
            this.showMetaFileName.read(configJson);
            this.lastModpackPath.read(configJson);
            this.zoomFactor.read(configJson);
        }
    }

    public void write(String reason) throws IOException {
        Files.createDirectories(CONFIG_DIR_PATH);

        JsonObject jsonObject = new JsonObject();

        JsonArray lastPickedFilesJsonArray = new JsonArray();
        for(Map.Entry<String, Path> entry : fileChooserLastPath.entrySet()) {
            JsonObject entryJsonObject = new JsonObject();
            entryJsonObject.addProperty("context", entry.getKey());
            entryJsonObject.addProperty("path", entry.getValue().toString());
            lastPickedFilesJsonArray.add(entryJsonObject);
        }
        jsonObject.add("lastPickedFiles", lastPickedFilesJsonArray);
        if(lastModpackPath.valueNotNull()) {
            jsonObject.addProperty(lastModpackPath.getKey(), lastModpackPath.getValue().toString());
        }
        jsonObject.addProperty(applicationTheme.getKey(), applicationTheme.getValue().name());
        jsonObject.addProperty(authorName.getKey(), authorName.getValue());
        jsonObject.addProperty(openLastModpackOnLaunch.getKey(), openLastModpackOnLaunch.getValue());
        jsonObject.addProperty(debugMode.getKey(), debugMode.getValue());
        jsonObject.addProperty(useWindowDecoration.getKey(), useWindowDecoration.getValue());
        jsonObject.addProperty(showMetaFileName.getKey(), showMetaFileName.getValue());
        jsonObject.addProperty(zoomFactor.getKey(), zoomFactor.getValue());

        JsonObject executableJsonObject = new JsonObject();
        if(packwizExecutablePath.valueNotNull()) {
            executableJsonObject.addProperty(packwizExecutablePath.getKey(), packwizExecutablePath.getValue().toString());
        }
        jsonObject.add("executables", executableJsonObject);

        try(Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject, writer);
        }
        super.write(reason);
    }

    public void setLastModpackPath(Path newValue) {
        if(!Objects.equals(newValue, this.lastModpackPath.getValue())) {
            this.lastModpackPath.setValue(newValue);
            try { // Write if changed
                write("Save last opened modpack path");
            } catch (IOException e) {
                PWGUI.LOGGER.error("Failed to save last opened modpack path!", e);
            }
        }
    }

    public static class ConfigEntry<T> {
        private final String configName;
        private final Function<JsonElement, T> valueSupplier;
        private T value;

        public ConfigEntry(String configName, T defaultValue, Function<JsonElement, T> valueSupplier) {
            this.configName = configName;
            this.value = defaultValue;
            this.valueSupplier = valueSupplier;
        }

        public void read(JsonObject jsonObject) {
            if(jsonObject.has(configName)) {
                try {
                    this.value = valueSupplier.apply(jsonObject.get(configName));
                } catch (IllegalArgumentException e) {
                    PWGUI.LOGGER.error("", e);
                }
            }
        }

        public String getKey() {
            return this.configName;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T newValue) {
            this.value = newValue;
        }

        public boolean valueNotNull() {
            return this.value != null;
        }

        @Override
        public String toString() {
            return getKey() + "/" + this.value;
        }
    }
}

