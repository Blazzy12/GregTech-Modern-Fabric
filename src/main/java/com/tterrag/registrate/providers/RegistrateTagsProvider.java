package com.tterrag.registrate.providers;

import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;
import com.gregtechceu.gtceu.compat.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Datagen-only tag provider. On Fabric tags come from the pre-generated data, so this only records calls. */
public interface RegistrateTagsProvider<T> extends RegistrateLookupFillerProvider {

    TagAppender<T> addTag(TagKey<T> tag);

    ResourceKey<? extends Registry<T>> registry();

    class TagAppender<T> {

        @SafeVarargs
        public final TagAppender<T> add(ResourceKey<T>... keys) {
            return this;
        }

        public TagAppender<T> add(T value) {
            return this;
        }

        public TagAppender<T> addTag(TagKey<T> tag) {
            return this;
        }

        public TagAppender<T> addOptional(Identifier id) {
            return this;
        }

        public TagAppender<T> addOptionalTag(Identifier id) {
            return this;
        }

        public TagAppender<T> addOptionalTag(TagKey<T> tag) {
            return this;
        }

        public TagAppender<T> remove(ResourceKey<T> key) {
            return this;
        }
    }

    class Impl<T> implements RegistrateTagsProvider<T> {

        private final ProviderType<? extends Impl<T>> type;
        private final String name;
        private final ResourceKey<? extends Registry<T>> registry;
        private final CompletableFuture<HolderLookup.Provider> lookup;

        public Impl(AbstractRegistrate<?> owner, ProviderType<? extends Impl<T>> type, String name,
                    PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn,
                    CompletableFuture<HolderLookup.Provider> registriesLookup, ExistingFileHelper existingFileHelper) {
            this.type = type;
            this.name = name;
            this.registry = registryIn;
            this.lookup = registriesLookup;
        }

        @Override
        public String getName() {
            return "Tags (" + name + ")";
        }

        @Override
        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        @Override
        public TagAppender<T> addTag(TagKey<T> tag) {
            return new TagAppender<>();
        }

        @Override
        public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
            return lookup;
        }

        @Override
        public ResourceKey<? extends Registry<T>> registry() {
            return registry;
        }
    }

    class IntrinsicImpl<T> extends Impl<T> {

        public IntrinsicImpl(AbstractRegistrate<?> owner, ProviderType<? extends IntrinsicImpl<T>> type, String name,
                             PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn,
                             CompletableFuture<HolderLookup.Provider> registriesLookup,
                             Function<T, ResourceKey<T>> keyExtractor, ExistingFileHelper existingFileHelper) {
            super(owner, type, name, packOutput, registryIn, registriesLookup, existingFileHelper);
        }
    }
}
