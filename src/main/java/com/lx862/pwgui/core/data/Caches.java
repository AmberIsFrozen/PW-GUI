package com.lx862.pwgui.core.data;

import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.data.VersionMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Caches {
    public static final Map<PackComponent, List<VersionMetadata>> componentCaches = new HashMap<>();
    public static final Map<String, byte[]> resourceCaches = new HashMap<>();

    public static CompletableFuture<List<VersionMetadata>> fetchVersionMetadata(PackComponent component) {
        if(componentCaches.containsKey(component)) {
            return CompletableFuture.completedFuture(componentCaches.get(component));
        } else {
            try {
                return component.versionGetter.get().thenApply(versionList -> {
                    Caches.componentCaches.put(component, versionList);
                    return versionList;
                });
            } catch (Exception e) {
                Caches.componentCaches.put(component, null);
                return CompletableFuture.completedFuture(null);
            }
        }
    }
}
