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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface VersionGetter {
    CompletableFuture<List<VersionMetadata>> get() throws MalformedURLException;

    static CompletableFuture<List<VersionMetadata>> fetchMinecraft() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String content = NetworkHelper.getRequestToString("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                JsonArray versionsArray = jsonObject.get("versions").getAsJsonArray();
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionsArray.size(); i++) {
                    JsonObject versionObject = versionsArray.get(i).getAsJsonObject();
                    String versionType = versionObject.get("type").getAsString();
                    VersionMetadata metadata = new VersionMetadata(null, versionObject.get("id").getAsString(), versionType.equals("snapshot") ? VersionMetadata.State.ALPHA : VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
                return metadatas;
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                return null;
            }
        }, PWGUI.BACKGROUND_EXECUTOR);
    }

    static CompletableFuture<List<VersionMetadata>> fetchFabric() throws MalformedURLException {
        return fetchFabricDerivatives("https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml", false);
    }

    static CompletableFuture<List<VersionMetadata>> fetchQuilt() throws MalformedURLException {
        return fetchFabricDerivatives("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-loader/maven-metadata.xml", false);
    }

    static CompletableFuture<List<VersionMetadata>> fetchLiteloader() throws MalformedURLException {
        return fetchFabricDerivatives("https://repo.mumfrey.com/content/repositories/snapshots/com/mumfrey/liteloader/maven-metadata.xml", true);
    }

    static CompletableFuture<List<VersionMetadata>> fetchFabricDerivatives(String url, boolean mcVersionLabeled) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String content = NetworkHelper.getRequestToString(url);
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                NodeList versionList = doc.getElementsByTagName("version");
                List<VersionMetadata> metadatas = new ArrayList<>();
                for(int i = 0; i < versionList.getLength(); i++) {
                    Node node = versionList.item((versionList.getLength()-1) - i); // Revert the list, since we want the newest to be 1st, and oldest to be last
                    String version = node.getTextContent();
                    String mcVersion = mcVersionLabeled ? version.split("-")[0] : null;
                    VersionMetadata metadata = new VersionMetadata(mcVersion, version, version.contains("beta") ? VersionMetadata.State.BETA : version.contains("alpha") ? VersionMetadata.State.ALPHA : VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
                return metadatas;
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                return null;
            }
        }, PWGUI.BACKGROUND_EXECUTOR);
    }

    static CompletableFuture<List<VersionMetadata>> fetchForge() throws MalformedURLException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                String content = NetworkHelper.getRequestToString("https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml");
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
                return metadatas;
            } catch (Exception e) {
                PWGUI.LOGGER.error("", e);
                return null;
            }
        }, PWGUI.BACKGROUND_EXECUTOR);
    }

    static CompletableFuture<List<VersionMetadata>> fetchNeoForge() throws MalformedURLException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VersionMetadata> metadatas = new ArrayList<>();

                List<VersionMetadata> metadata12001 = fetchNeoForgeInternal("https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml", true);
                if(metadata12001 != null) metadatas.addAll(metadata12001);
                return metadatas;
            } catch (MalformedURLException e) {
                return null;
            }
        }, PWGUI.BACKGROUND_EXECUTOR)
        .thenApplyAsync(metadatas -> {
            if(metadatas == null) return metadatas;

            try {
                List<VersionMetadata> metadata12002 = fetchNeoForgeInternal("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml", false);
                if(metadata12002 != null) metadatas.addAll(metadata12002);
                Collections.reverse(metadatas); // NeoForge sorts from oldest to newest
                return metadatas;
            } catch (MalformedURLException e) {
                return null;
            }
        }, PWGUI.BACKGROUND_EXECUTOR);
    }

    static List<VersionMetadata> fetchNeoForgeInternal(String url, boolean isFor12001) throws MalformedURLException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            String content = NetworkHelper.getRequestToString(url);
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
                    String[] versionComponents = version.split("\\.");
                    String mcVersionMajor = versionComponents[0];
                    String mcVersionMinor = versionComponents[1];
                    boolean is26x = mcVersionMajor.equals("26");
                    if(is26x && !Objects.equals(versionComponents[2], "0")) {
                        mcVersionMinor += "." + versionComponents[2];
                    }
                    String mcVersion = (is26x ? "" : "1.") + mcVersionMajor + (mcVersionMinor.equals("0") ? "" : "." + mcVersionMinor);
                    VersionMetadata metadata = new VersionMetadata(mcVersion, version, version.contains("beta") ? VersionMetadata.State.BETA : VersionMetadata.State.RELEASE);
                    metadatas.add(metadata);
                }
            }
            return metadatas;
        } catch (Exception e) {
            PWGUI.LOGGER.error("", e);
            return null;
        }
    }
}
