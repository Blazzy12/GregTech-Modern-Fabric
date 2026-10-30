package com.gregtechceu.gtceu.compat.spi.language;

import net.fabricmc.loader.api.ModContainer;

public class IModInfo {

    private final ModContainer container;

    public IModInfo(ModContainer container) {
        this.container = container;
    }

    public String getModId() {
        return container.getMetadata().getId();
    }

    public String getDisplayName() {
        return container.getMetadata().getName();
    }

    public String getVersion() {
        return container.getMetadata().getVersion().getFriendlyString();
    }

    public ModContainer getContainer() {
        return container;
    }
}
