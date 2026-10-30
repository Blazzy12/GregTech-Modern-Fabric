package brachy.modularui.utils;

import com.gregtechceu.gtceu.compat.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSources;
import com.gregtechceu.gtceu.compat.common.conditions.ICondition;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(fluent = true)
public final class RegistryAccessContainer implements ICondition.IContext, RegistryAccess.Frozen {

    public static final RegistryAccessContainer BUILTIN = new RegistryAccessContainer(
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    @Getter
    private static RegistryAccessContainer current = BUILTIN;
    private static ICondition.IContext tagContext = ICondition.IContext.TAGS_INVALID;

    @Getter
    private final RegistryAccess access;
    private DamageSources damageSources;

    public RegistryAccessContainer(RegistryAccess access) {
        this.access = access;
        this.damageSources = null;
    }

    @ApiStatus.Internal
    public static void update(RegistryAccess registries, @Nullable ICondition.IContext tagContext) {
        RegistryAccessContainer.current = new RegistryAccessContainer(registries);
        if (tagContext != null) {
            RegistryAccessContainer.tagContext = tagContext;
        }
    }

    /** Updates the current registries, taking the tags from the registries themselves. */
    @ApiStatus.Internal
    public static void update(RegistryAccess registries) {
        update(registries, new ICondition.IContext() {

            @Override
            public <T> Map<Identifier, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key) {
                return registries.lookup(key)
                        .map(registry -> registry.getTags().collect(Collectors.toMap(
                                named -> named.key().location(),
                                named -> (Collection<Holder<T>>) named.stream().toList(),
                                (a, b) -> a)))
                        .orElse(Map.of());
            }
        });
    }

    public DamageSources damageSources() {
        if (damageSources == null) {
            damageSources = new DamageSources(access);
        }

        return damageSources;
    }

    // region tag context methods

    @Override
    public <T> Map<Identifier, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
        return tagContext.getAllTags(registry);
    }

    // endregion

    // region registry access methods

    @Override
    public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return access.lookup(registryKey);
    }

    @Override
    public Stream<RegistryEntry<?>> registries() {
        return access.registries();
    }

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return access.listRegistryKeys();
    }

    // endregion
}
