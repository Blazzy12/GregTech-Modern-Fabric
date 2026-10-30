package com.tterrag.registrate.providers;

import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider.IntrinsicImpl<Item> {

    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type,
                                      String name, PackOutput packOutput,
                                      CompletableFuture<HolderLookup.Provider> registriesLookup,
                                      Object blockTags, ExistingFileHelper existingFileHelper) {
        super(owner, type, name, packOutput, Registries.ITEM, registriesLookup,
                item -> item.builtInRegistryHolder().key(), existingFileHelper);
    }

    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {}
}
