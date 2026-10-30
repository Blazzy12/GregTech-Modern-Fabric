package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagUtil {

    private static final java.util.Map<ResourceKey<?>, java.util.Map<? extends TagKey<?>, ? extends java.util.List<?>>> PENDING_CONTENTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<TagKey<?>, net.minecraft.core.HolderSet.Named<?>> PENDING_SETS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<TagKey<?>> MISSING_TAGS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile boolean generating;

    /** Called as a registry prepares to swap in reloaded tags. */
    public static <T> void capturePendingTags(net.minecraft.tags.TagLoader.LoadResult<T> tags) {
        PENDING_CONTENTS.put(tags.key(), tags.tags());
    }

    /**
     * Runs {@code action} with {@link #holderSet} handing out the tags of the data reload in progress, which 26.x
     * only binds once loading is done, so code that builds recipes while data loads can read them.
     */
    public static void withPendingTags(Runnable action) {
        PENDING_SETS.clear();
        generating = true;
        try {
            action.run();
        } finally {
            generating = false;
            PENDING_SETS.clear();
            if (!MISSING_TAGS.isEmpty()) {
                GTCEu.LOGGER.warn("Recipe generation used {} tags nothing defines, e.g. {}", MISSING_TAGS.size(),
                        MISSING_TAGS.stream().limit(20).toList());
                MISSING_TAGS.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> net.minecraft.core.HolderSet.Named<T> pendingSet(Registry<T> registry, TagKey<T> tag) {
        return (net.minecraft.core.HolderSet.Named<T>) PENDING_SETS.computeIfAbsent(tag, k -> {
            var contents = (java.util.Map<TagKey<T>, java.util.List<net.minecraft.core.Holder<T>>>) PENDING_CONTENTS
                    .get(registry.key());
            // a tag nothing defines is empty, as it was on 1.21.1
            var list = contents == null ? null : contents.get(tag);
            if (list == null) MISSING_TAGS.add(tag);
            var members = list == null ? java.util.Set.<net.minecraft.core.Holder<T>>of() : java.util.Set.copyOf(list);
            // membership normally asks the holder for its tags, which are not bound yet
            var set = new net.minecraft.core.HolderSet.Named<T>(registry, tag) {

                @Override
                public boolean contains(net.minecraft.core.Holder<T> holder) {
                    return members.contains(holder);
                }
            };
            set.bind(list == null ? java.util.List.of() : list);
            return set;
        });
    }

    /**
     * Creates a tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Tag {@code #c:path} or {@code #minecraft:path}
     */
    /**
     * The registry's named set for a tag. 26.x registries only hand out tags that exist, and GT builds recipe
     * ingredients before tags load, so a missing tag becomes an unbound set that still serialises by its key.
     */
    public static <T> net.minecraft.core.HolderSet.Named<T> holderSet(Registry<T> registry, TagKey<T> tag) {
        // while data loads, the tags just read are not bound to the registry yet, so hand out bound copies
        if (generating) {
            return pendingSet(registry, tag);
        }
        // during registration the registry hands out (and later binds) any tag
        if (registry instanceof net.minecraft.core.MappedRegistry<T> mapped && !mapped.frozen) {
            return mapped.createRegistrationLookup().getOrThrow(tag);
        }
        try {
            return registry.get(tag).orElseGet(() -> net.minecraft.core.HolderSet.emptyNamed(registry, tag));
        } catch (IllegalStateException tagsNotBound) {
            return net.minecraft.core.HolderSet.emptyNamed(registry, tag);
        }
    }

    public static <T> TagKey<T> createTag(ResourceKey<? extends Registry<T>> registryKey, String path,
                                          boolean vanilla) {
        if (vanilla) return TagKey.create(registryKey, Identifier.withDefaultNamespace(path));
        return TagKey.create(registryKey, Identifier.fromNamespaceAndPath("c", path));
    }

    /**
     * Creates a tag under the {@code gtceu} namespace
     * 
     * @return {@code #gtceu:path}
     */
    public static <T> TagKey<T> createModTag(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return TagKey.create(registryKey, GTCEu.id(path));
    }

    /**
     * Creates a block tag under the {@code c} namespace
     * 
     * @return Block tag {@code #c:path}
     */
    public static TagKey<Block> createBlockTag(String path) {
        return createTag(Registries.BLOCK, path, false);
    }

    /**
     * Creates a block tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Block tag {@code #c:path} or {@code #minecraft:path}
     */
    public static TagKey<Block> createBlockTag(String path, boolean vanilla) {
        return createTag(Registries.BLOCK, path, vanilla);
    }

    /**
     * Creates a block tag under the {@code gtceu} namespace
     *
     * @return Block tag {@code #gtceu:path}
     */
    public static TagKey<Block> createModBlockTag(String path) {
        return createModTag(Registries.BLOCK, path);
    }

    /**
     * Creates an item tag under the {@code c} namespace
     * 
     * @return Item tag {@code #c:path}
     */
    public static TagKey<Item> createItemTag(String path) {
        return createTag(Registries.ITEM, path, false);
    }

    /**
     * Creates an item tag under the {@code c} or {@code minecraft} namespace
     * 
     * @param vanilla Whether to use vanilla namespace instead of common
     * @return Item tag {@code #c:path} or {@code #minecraft:path}
     */
    public static TagKey<Item> createItemTag(String path, boolean vanilla) {
        return createTag(Registries.ITEM, path, vanilla);
    }

    /**
     * Creates an item tag under the {@code gtceu} namespace
     * 
     * @return Item tag {@code #gtceu:path}
     */
    public static TagKey<Item> createModItemTag(String path) {
        return createModTag(Registries.ITEM, path);
    }

    /**
     * Creates a fluid tag under the {@code c} namespace
     * 
     * @return Fluid tag {@code #c:path}
     */
    public static TagKey<Fluid> createFluidTag(String path) {
        return createTag(Registries.FLUID, path, false);
    }

    /**
     * Generates fluid tag under GTM namespace
     *
     * @return Fluid tag #gtceu:path
     */
    public static TagKey<Fluid> createModFluidTag(String path) {
        return createModTag(Registries.FLUID, path);
    }

    public static <T> TagKey<T> optionalTag(ResourceKey<? extends Registry<T>> registryKey, Identifier path) {
        return TagKey.create(registryKey, path);
    }
}
