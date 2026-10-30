package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.registries.NeoForgeRegistries;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Matches fluid stacks, ignoring amount. Serialized as {@code {"fluid": ...}}, {@code {"tag": ...}}, a list of
 * ingredients, or {@code {"type": ..., ...}} for other types.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {

    private static final Codec<FluidIngredient> DISPATCH_CODEC = Codec.lazyInitialized(
            () -> NeoForgeRegistries.FLUID_INGREDIENT_TYPES.byNameCodec()
                    .dispatch("type", FluidIngredient::getType, FluidIngredientType::codec));

    private static final MapCodec<FluidIngredient> SIMPLE_MAP_CODEC = Codec.mapEither(
            SingleFluidIngredient.CODEC, TagFluidIngredient.CODEC)
            .xmap(e -> e.map(s -> (FluidIngredient) s, t -> t), i -> i instanceof SingleFluidIngredient s ?
                    Either.left(s) : Either.right((TagFluidIngredient) i));

    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = makeMapCodec();

    public static final Codec<FluidIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    public static final Codec<List<FluidIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();

    public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = LIST_CODEC.validate(
            list -> list.isEmpty() ? DataResult.error(() -> "Fluid ingredient list cannot be empty") :
                    DataResult.success(list));

    public static final Codec<FluidIngredient> CODEC = codec(true);

    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = codec(false);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = ByteBufCodecs
            .fromCodecWithRegistries(Codec.lazyInitialized(() -> CODEC));

    private FluidStack[] stacks;

    private static MapCodec<FluidIngredient> makeMapCodec() {
        return Codec.mapEither(SIMPLE_MAP_CODEC, MapCodec.assumeMapUnsafe(Codec.lazyInitialized(() -> DISPATCH_CODEC)))
                .xmap(e -> e.map(i -> i, i -> i), i -> i instanceof SingleFluidIngredient ||
                        i instanceof TagFluidIngredient ? Either.left(i) : Either.right(i));
    }

    private static Codec<FluidIngredient> codec(boolean allowEmpty) {
        Codec<List<FluidIngredient>> listCodec = allowEmpty ? LIST_CODEC : LIST_CODEC_NON_EMPTY;
        return Codec.either(listCodec, MAP_CODEC_CODEC).xmap(
                either -> either.map(CompoundFluidIngredient::of, i -> i),
                ingredient -> {
                    if (ingredient instanceof CompoundFluidIngredient compound) {
                        return Either.left(compound.children());
                    }
                    if (ingredient.isEmpty()) return Either.left(List.of());
                    return Either.right(ingredient);
                });
    }

    /** All stacks this ingredient can match, each at one bucket. */
    public final FluidStack[] getStacks() {
        if (stacks == null) stacks = generateStacks().toArray(FluidStack[]::new);
        return stacks;
    }

    @Override
    public abstract boolean test(FluidStack fluidStack);

    protected abstract Stream<FluidStack> generateStacks();

    public abstract boolean isSimple();

    public abstract FluidIngredientType<?> getType();

    public final boolean isEmpty() {
        return this == EmptyFluidIngredient.INSTANCE;
    }

    public final boolean hasNoFluids() {
        return getStacks().length == 0;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public static FluidIngredient empty() {
        return EmptyFluidIngredient.INSTANCE;
    }

    public static FluidIngredient of() {
        return empty();
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    private static FluidIngredient of(Stream<Fluid> fluids) {
        return CompoundFluidIngredient.of(fluids.map(FluidIngredient::single).toList());
    }

    public static FluidIngredient single(FluidStack stack) {
        return single(stack.getFluid());
    }

    public static FluidIngredient single(Fluid fluid) {
        return single(fluid.builtInRegistryHolder());
    }

    public static FluidIngredient single(Holder<Fluid> holder) {
        return new SingleFluidIngredient(holder);
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }
}
