package com.gregtechceu.gtceu.compat.common.conditions;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

import java.util.Collection;
import java.util.Map;

/** Load conditions, kept for API compatibility. Fabric resource conditions are evaluated by Fabric itself. */
public interface ICondition {

    boolean test(IContext context);

    interface IContext {

        IContext EMPTY = new IContext() {

            @Override
            public <T> Map<net.minecraft.resources.Identifier, Collection<Holder<T>>> getAllTags(
                    net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> registry) {
                return Map.of();
            }
        };

        IContext TAGS_INVALID = EMPTY;

        <T> Map<net.minecraft.resources.Identifier, Collection<Holder<T>>> getAllTags(
                net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> registry);

        default <T> boolean isTagLoaded(TagKey<T> key) {
            return getAllTags(key.registry()).containsKey(key.location());
        }
    }
}
