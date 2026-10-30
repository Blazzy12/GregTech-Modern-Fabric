package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.event.AddPackFindersEvent;
import com.gregtechceu.gtceu.compat.fml.ModBuses;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lets GregTech add its pack sources (the dynamic resource and data packs) to every pack repository, like
 * NeoForge's AddPackFindersEvent. A repository with the vanilla server pack source holds data packs; the others
 * hold resource packs.
 */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Unique
    private boolean gtceu$packFindersAdded;

    /**
     * Sources are added at the first reload rather than in the constructor: the client's resource pack repository
     * is made before mods initialise.
     */
    @Inject(method = "reload", at = @At("HEAD"))
    private void gtceu$addPackFinders(CallbackInfo ci) {
        if (gtceu$packFindersAdded) return;
        gtceu$packFindersAdded = true;
        boolean data = false;
        for (RepositorySource source : this.sources) {
            if (source instanceof ServerPacksSource) data = true;
        }
        Set<RepositorySource> all = new LinkedHashSet<>(this.sources);
        ModBuses.MOD_BUS.post(new AddPackFindersEvent(data ? PackType.SERVER_DATA : PackType.CLIENT_RESOURCES,
                all::add));
        this.sources = all;
    }
}
