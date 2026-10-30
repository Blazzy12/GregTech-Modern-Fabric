package com.tterrag.registrate.providers;

import com.gregtechceu.gtceu.compat.fml.LogicalSide;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.util.concurrent.CompletableFuture;

public interface RegistrateProvider extends DataProvider {

    LogicalSide getSide();

    /** Datagen does not run on Fabric; providers only collect data that is never written. */
    @Override
    default CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.completedFuture(null);
    }
}
