package com.gregtechceu.gtceu.data.pack;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import com.mojang.serialization.Lifecycle;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Lookups for registries that do not exist yet when GT generates its data at the start of a reload (recipes, which
 * are no registry at all at runtime, and the loot predicates loaded with the loot tables). They hand out holders
 * that only know their key, which is all the generated JSON needs: serialised through {@link #with}, such holders
 * are written as their id.
 */
public final class KeyOnlyLookups {

    private static final Map<ResourceKey<?>, HolderLookup.RegistryLookup<?>> LOOKUPS = new ConcurrentHashMap<>();

    /** The registries GT's generated data refers to before they load. */
    private static final java.util.Set<ResourceKey<?>> KEY_ONLY = java.util.Set.of(Registries.RECIPE,
            Registries.PREDICATE, Registries.ITEM_MODIFIER, Registries.LOOT_TABLE);

    private KeyOnlyLookups() {}

    public static boolean isKeyOnly(ResourceKey<? extends Registry<?>> registry) {
        return KEY_ONLY.contains(registry);
    }

    @SuppressWarnings("unchecked")
    public static <T> HolderLookup.RegistryLookup<T> get(ResourceKey<? extends Registry<? extends T>> registry) {
        return (HolderLookup.RegistryLookup<T>) LOOKUPS.computeIfAbsent(registry, KeyOnlyLookups::create);
    }

    /** {@code registries}, with key-only lookups for the registries it lacks of those above. */
    public static HolderLookup.Provider with(HolderLookup.Provider registries) {
        return new HolderLookup.Provider() {

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return Stream.concat(registries.listRegistryKeys(),
                        KEY_ONLY.stream().map(k -> (ResourceKey<? extends Registry<?>>) k)).distinct();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(
                                                                                ResourceKey<? extends Registry<? extends T>> key) {
                Optional<? extends HolderLookup.RegistryLookup<T>> lookup = registries.lookup(key);
                if (lookup.isEmpty() && KEY_ONLY.contains(key)) {
                    return Optional.of(KeyOnlyLookups.<T>get(key));
                }
                return lookup;
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static HolderLookup.RegistryLookup<?> create(ResourceKey<?> registry) {
        return new HolderLookup.RegistryLookup() {

            @Override
            public ResourceKey key() {
                return (ResourceKey) registry;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return Lifecycle.stable();
            }

            @Override
            public Stream listElements() {
                return Stream.empty();
            }

            @Override
            public Stream listTags() {
                return Stream.empty();
            }

            @Override
            public Optional get(ResourceKey id) {
                return Optional.of(Holder.Reference.createStandAlone(this, id));
            }

            @Override
            public Optional get(TagKey tag) {
                return Optional.<HolderSet.Named>empty();
            }
        };
    }
}
