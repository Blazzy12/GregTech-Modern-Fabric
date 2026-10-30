package com.tterrag.registrate.providers;

import com.gregtechceu.gtceu.compat.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/** Datagen-only provider. Datagen does not run on Fabric; the data is pre-generated in src/generated. */
public class RegistrateDatapackProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> owner;

    public RegistrateDatapackProvider(AbstractRegistrate<?> owner, PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        this.owner = owner;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public String getName() {
        return "RegistrateDatapackProvider (" + owner.getModid() + ")";
    }
}
