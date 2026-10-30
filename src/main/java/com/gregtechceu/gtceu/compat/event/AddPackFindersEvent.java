package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;

/** Fired while a pack repository is created, so extra pack sources can be added. */
public class AddPackFindersEvent extends Event implements IModBusEvent {

    private final PackType packType;
    private final Consumer<RepositorySource> sources;

    public AddPackFindersEvent(PackType packType, Consumer<RepositorySource> sources) {
        this.packType = packType;
        this.sources = sources;
    }

    public PackType getPackType() {
        return packType;
    }

    public void addRepositorySource(RepositorySource source) {
        sources.accept(source);
    }
}
