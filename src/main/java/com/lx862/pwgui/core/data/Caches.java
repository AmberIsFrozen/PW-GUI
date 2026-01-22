package com.lx862.pwgui.core.data;

import com.lx862.pwgui.support.packwiz.data.PackComponent;
import com.lx862.pwgui.support.packwiz.data.VersionMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Caches {
    public static final HashMap<PackComponent, List<VersionMetadata>> componentCaches = new HashMap<>();

    public static void getVersionMetadata(PackComponent component, Consumer<List<VersionMetadata>> callback) {
        if(componentCaches.containsKey(component)) {
            callback.accept(componentCaches.get(component));
        } else {
            try {
                component.versionGetter.get((versionList) -> {
                    Caches.componentCaches.put(PackComponent.MINECRAFT, versionList);
                    callback.accept(versionList);
                });
            } catch (Exception e) {
                Caches.componentCaches.put(component, null);
            }
        }
    }
}
