package com.gregtechceu.gtceu.compat.fml;

import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.spi.language.IModInfo;

import net.fabricmc.loader.api.FabricLoader;

import org.jetbrains.annotations.Nullable;

public class ModContainer {

    private final net.fabricmc.loader.api.ModContainer fabric;
    private final IModInfo info;

    public ModContainer(net.fabricmc.loader.api.ModContainer fabric) {
        this.fabric = fabric;
        this.info = new IModInfo(fabric);
    }

    public String getModId() {
        return fabric.getMetadata().getId();
    }

    public IModInfo getModInfo() {
        return info;
    }

    public net.fabricmc.loader.api.ModContainer getFabricContainer() {
        return fabric;
    }

    /** Only GregTech's own bus exists. Other mods receive it too so addons can listen to GT events. */
    public @Nullable IEventBus getEventBus() {
        return ModBuses.MOD_BUS;
    }

    static ModContainer of(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(ModContainer::new).orElse(null);
    }
}
