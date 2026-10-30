package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.compat.fml.ModList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.util.*;

public final class AddonFinder {

    private static final Logger LOGGER = LogManager.getLogger("GTCEu Addon Finder");
    private static final List<IGTAddon> cache = new ArrayList<>();
    private static Map<String, IGTAddon> modIdMap = null;

    @UnmodifiableView
    public static List<IGTAddon> getAddonList() {
        ensureInitialized();
        return Collections.unmodifiableList(cache);
    }

    @UnmodifiableView
    public static Map<String, IGTAddon> getAddons() {
        ensureInitialized();
        return Collections.unmodifiableMap(modIdMap);
    }

    @Nullable
    public static IGTAddon getAddon(String modId) {
        return modIdMap.get(modId);
    }

    private static void ensureInitialized() {
        if (modIdMap == null) {
            modIdMap = getInstances();
            cache.addAll(modIdMap.values());
        }
    }

    /**
     * Fabric has no annotation scan data, so addons declare a {@code "gtceu:addon"} entrypoint implementing
     * {@link IGTAddon} in their {@code fabric.mod.json}.
     */
    private static Map<String, IGTAddon> getInstances() {
        Map<String, IGTAddon> instances = new LinkedHashMap<>();
        for (var container : net.fabricmc.loader.api.FabricLoader.getInstance()
                .getEntrypointContainers("gtceu:addon", IGTAddon.class)) {
            String modId = container.getProvider().getMetadata().getId();
            try {
                instances.put(modId, container.getEntrypoint());
            } catch (Throwable e) {
                LOGGER.error("Failed to load GT addon entrypoint from {}", modId, e);
            }
        }
        return instances;
    }
}
