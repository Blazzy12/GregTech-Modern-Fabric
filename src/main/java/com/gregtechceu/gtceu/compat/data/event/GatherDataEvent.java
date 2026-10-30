package com.gregtechceu.gtceu.compat.data.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;

import java.util.concurrent.CompletableFuture;

/**
 * NeoForge's datagen event. It is never fired on Fabric, where GT's resources are pre-generated.
 */
public class GatherDataEvent extends Event implements IModBusEvent {

    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public GatherDataEvent(DataGenerator generator, ExistingFileHelper existingFileHelper,
                           CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.generator = generator;
        this.existingFileHelper = existingFileHelper;
        this.lookupProvider = lookupProvider;
    }

    public DataGenerator getGenerator() {
        return generator;
    }

    /** The vanilla pack output of the generator (26.x keeps it protected on {@code DataGenerator}). */
    public net.minecraft.data.PackOutput getPackOutput() {
        return generator.vanillaPackOutput;
    }

    public ExistingFileHelper getExistingFileHelper() {
        return existingFileHelper;
    }

    public CompletableFuture<HolderLookup.Provider> getLookupProvider() {
        return lookupProvider;
    }

    public boolean includeClient() {
        return true;
    }

    public boolean includeServer() {
        return true;
    }
}
