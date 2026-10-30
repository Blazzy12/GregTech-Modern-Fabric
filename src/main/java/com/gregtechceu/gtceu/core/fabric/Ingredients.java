package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/** Ingredient helpers that 26.x dropped. */
public final class Ingredients {

    private Ingredients() {}

    /**
     * Stand-in for 1.21.1's {@code Ingredient.EMPTY}: 26.x ingredients cannot be built from an empty item list, so
     * this is a tag ingredient over an always-empty tag. It matches nothing and reports {@code isEmpty()}. The tag ships as
     * {@code data/gtceu/tags/item/empty.json} so recipes that serialize it (e.g. equipment foundry tiers whose items
     * are not registered when high-tier content is off) still parse.
     */
    public static final Ingredient EMPTY = createEmpty();

    private static Ingredient createEmpty() {
        HolderSet.Named<Item> set = HolderSet.emptyNamed(BuiltInRegistries.ITEM,
                TagKey.create(Registries.ITEM, GTCEu.id("empty")));
        set.bind(List.of());
        return Ingredient.of(set);
    }
}
