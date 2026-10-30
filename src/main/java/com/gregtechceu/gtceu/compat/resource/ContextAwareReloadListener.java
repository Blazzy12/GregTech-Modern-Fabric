package com.gregtechceu.gtceu.compat.resource;

import net.minecraft.core.HolderLookup;

import org.jetbrains.annotations.Nullable;

/** A reload listener that is given the registry lookup of the reload in progress. */
public abstract class ContextAwareReloadListener {

    private @Nullable HolderLookup.Provider registryLookup;

    public void injectContext(HolderLookup.Provider registryLookup) {
        this.registryLookup = registryLookup;
    }

    protected final HolderLookup.Provider getRegistryLookup() {
        if (registryLookup == null) throw new IllegalStateException("Registry lookup not injected");
        return registryLookup;
    }
}
