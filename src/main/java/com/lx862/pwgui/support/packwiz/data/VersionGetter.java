package com.lx862.pwgui.support.packwiz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.util.NetworkHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface VersionGetter {
    void get(Consumer<List<VersionMetadata>> callback) throws MalformedURLException;

    static void fetchMinecraft(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        URL url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
        CompletableFuture.runAsync(() -> {
            try {
                String content = NetworkHelper.getFromURL(url);
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                JsonArray versionsArray = jsonObject.get("versions").getAsJsonArray();
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionsArray.size(); i++) {
                    JsonObject versionObject = versionsArray.get(i).getAsJsonObject();
                    String versionType = versionObject.get("type").getAsString();
                    VersionMetadata metadata = new VersionMetadata(null, versionObject.get("id").getAsString(), versionType.equals("snapshot") ? VersionMetadata.State.ALPHA : VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
                callback.accept(metadatas);
            } catch (IOException e) {
                callback.accept(null);
                PWGUI.LOGGER.error("", e);
            }
        });
    }

    static void fetchFabric(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        fetchFabricDerivatives("https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml", false, callback);
    }

    static void fetchQuilt(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        fetchFabricDerivatives("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-loader/maven-metadata.xml", false, callback);
    }

    static void fetchLiteloader(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        fetchFabricDerivatives("https://repo.mumfrey.com/content/repositories/snapshots/com/mumfrey/liteloader/maven-metadata.xml", true, callback);
    }

    static void fetchFabricDerivatives(String urlString, boolean mcVersionLabeled, Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        URL url = new URL(urlString);
        CompletableFuture.runAsync(() -> {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String content = NetworkHelper.getFromURL(url);
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                NodeList versionList = doc.getElementsByTagName("version");
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionList.getLength(); i++) {
                    Node node = versionList.item((versionList.getLength()-1) - i); // Revert the list, since we want the newest to be 1st, and oldest to be last
                    String version = node.getTextContent();
                    String mcVersion = mcVersionLabeled ? version.split("-")[0] : null;
                    VersionMetadata metadata = new VersionMetadata(mcVersion, version, version.contains("beta") ? VersionMetadata.State.BETA : VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
                callback.accept(metadatas);
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                callback.accept(null);
            }
        });
    }

    static void fetchForge(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        URL url = new URL("https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml");
        CompletableFuture.runAsync(() -> {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String content = NetworkHelper.getFromURL(url);
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                NodeList versionList = doc.getElementsByTagName("version");
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionList.getLength(); i++) {
                    Node node = versionList.item(i);
                    String mcVersion = node.getTextContent().split("-")[0];
                    String modloaderVersion = node.getTextContent().split("-")[1];
                    VersionMetadata metadata = new VersionMetadata(mcVersion, modloaderVersion, VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
                callback.accept(metadatas);
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                callback.accept(null);
            }
        });
    }

    static void fetchNeoForge(Consumer<List<VersionMetadata>> callback) throws MalformedURLException {
        List<VersionMetadata> metadatas = new ArrayList<>();
        fetchNeoForgeInternal("https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml", (metadata12001) -> {
            metadatas.addAll(metadata12001);

            try {
                fetchNeoForgeInternal("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml", (metadata12002) -> {
                    metadatas.addAll(metadata12002);
                    Collections.reverse(metadatas); // NeoForge sorts from oldest to newest
                    callback.accept(metadatas);
                }, false);
            } catch (MalformedURLException e) {
                PWGUI.LOGGER.error("", e);
                callback.accept(null);
            }
        }, true);
    }

    static void fetchNeoForgeInternal(String urlString, Consumer<List<VersionMetadata>> callback, boolean isFor12001) throws MalformedURLException {
        URL url = new URL(urlString);
        CompletableFuture.runAsync(() -> {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String content = NetworkHelper.getFromURL(url);
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                NodeList versionList = doc.getElementsByTagName("version");
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionList.getLength(); i++) {
                    Node node = versionList.item(i);
                    String version = node.getTextContent();
                    if(isFor12001) {
                        VersionMetadata metadata = new VersionMetadata("1.20.1", version.contains("-") ? version.split("-")[1] : version, VersionMetadata.State.RELEASE);
                        metadatas.add(metadata);
                    } else {
                        String mcVersionMajor = version.split("\\.")[0];
                        String mcVersionMinor = version.split("\\.")[1];
                        String mcVersion = "1." + mcVersionMajor + (mcVersionMinor.equals("0") ? "" : "." + mcVersionMinor);
                        VersionMetadata metadata = new VersionMetadata(mcVersion, version, version.contains("beta") ? VersionMetadata.State.BETA : VersionMetadata.State.RELEASE);
                        metadatas.add(metadata);
                    }
                }
                callback.accept(metadatas);
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                callback.accept(null);
            }
        });
    }
}
