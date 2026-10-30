package com.gregtechceu.gtceu.compat.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A quantity of fluid with data components, measured in millibuckets. Mirrors NeoForge's FluidStack.
 */
public final class FluidStack implements DataComponentHolder {

    private static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
            .validate(holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder()) ?
                    DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(holder));

    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
            i -> i.group(
                    FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                    ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(stack -> stack.components.asPatch()))
                    .apply(i, FluidStack::new)));

    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(FluidStack.EMPTY), s -> s.isEmpty() ? Optional.empty() : Optional.of(s));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {

        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs
                .holderRegistry(net.minecraft.core.registries.Registries.FLUID);

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) return FluidStack.EMPTY;
            Holder<Fluid> fluid = FLUID_STREAM_CODEC.decode(buf);
            DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            return new FluidStack(fluid, amount, patch);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                buf.writeVarInt(0);
                return;
            }
            buf.writeVarInt(stack.getAmount());
            FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
            DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            FluidStack stack = OPTIONAL_STREAM_CODEC.decode(buf);
            if (stack.isEmpty()) throw new DecoderException("Empty FluidStack not allowed");
            return stack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) throw new EncoderException("Empty FluidStack not allowed");
            OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    };

    public static final FluidStack EMPTY = new FluidStack(null);

    private final @Nullable Fluid fluid;
    private int amount;
    private final PatchedDataComponentMap components;

    public static Codec<FluidStack> fixedAmountCodec(int amount) {
        return RecordCodecBuilder.create(i -> i.group(
                FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                        .forGetter(stack -> stack.components.asPatch()))
                .apply(i, (holder, patch) -> new FluidStack(holder, amount, patch)));
    }

    public static MapCodec<FluidStack> lenientOptionalFieldOf(String name) {
        return CODEC.lenientOptionalFieldOf(name).xmap(o -> o.orElse(EMPTY),
                s -> s.isEmpty() ? Optional.empty() : Optional.of(s));
    }

    private FluidStack(@Nullable Void unused) {
        this.fluid = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid.value(), amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
    }

    private FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == null || fluid == Fluids.EMPTY || amount <= 0;
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public Holder<Fluid> getFluidHolder() {
        return getFluid().builtInRegistryHolder();
    }

    public FluidType getFluidType() {
        return FluidType.of(getFluid());
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (this == EMPTY) throw new UnsupportedOperationException("Cannot modify the empty stack");
        this.amount = amount;
    }

    public void limitSize(int max) {
        if (!isEmpty() && amount > max) setAmount(max);
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        grow(-amount);
    }

    public boolean is(TagKey<Fluid> tag) {
        return getFluidHolder().is(tag);
    }

    public boolean is(Fluid other) {
        return getFluid() == other;
    }

    public boolean is(Predicate<Holder<Fluid>> predicate) {
        return predicate.test(getFluidHolder());
    }

    public boolean is(Holder<Fluid> holder) {
        return getFluid() == holder.value();
    }

    public boolean is(net.minecraft.core.HolderSet<Fluid> set) {
        return set.contains(getFluidHolder());
    }

    public Stream<TagKey<Fluid>> getTags() {
        return getFluidHolder().tags();
    }

    @Override
    public PatchedDataComponentMap getComponents() {
        return components;
    }

    public DataComponentPatch getComponentsPatch() {
        return isEmpty() ? DataComponentPatch.EMPTY : components.asPatch();
    }

    public DataComponentMap immutableComponents() {
        return components.toImmutableMap();
    }

    public boolean isComponentsPatchEmpty() {
        return components.asPatch().isEmpty();
    }

    public <T> @Nullable T set(DataComponentType<T> type, @Nullable T value) {
        return components.set(type, value);
    }

    public <T> @Nullable T set(java.util.function.Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return set(type.get(), value);
    }

    public <T> @Nullable T remove(DataComponentType<? extends T> type) {
        return components.remove(type);
    }

    public <T> @Nullable T update(DataComponentType<T> type, T defaultValue,
                                  java.util.function.UnaryOperator<T> updater) {
        return set(type, updater.apply(getOrDefault(type, defaultValue)));
    }

    public <T, U> @Nullable T update(DataComponentType<T> type, T defaultValue, U value,
                                     java.util.function.BiFunction<T, U, T> updater) {
        return set(type, updater.apply(getOrDefault(type, defaultValue), value));
    }

    public void applyComponents(DataComponentPatch patch) {
        components.applyPatch(patch);
    }

    public void applyComponents(DataComponentMap map) {
        components.setAll(map);
    }

    public FluidStack copy() {
        if (isEmpty()) return EMPTY;
        return new FluidStack(fluid, amount, components.copy());
    }

    public FluidStack copyWithAmount(int amount) {
        if (isEmpty()) return EMPTY;
        FluidStack copy = copy();
        copy.setAmount(amount);
        return copy;
    }

    public FluidStack copyAndClear() {
        if (isEmpty()) return EMPTY;
        FluidStack copy = copy();
        setAmount(0);
        return copy;
    }

    public FluidStack split(int amount) {
        int taken = Math.min(amount, this.amount);
        FluidStack result = copyWithAmount(taken);
        shrink(taken);
        return result;
    }

    /** The hover name in square brackets, like {@code ItemStack#getDisplayName}. */
    public Component getDisplayName() {
        return net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(getHoverName());
    }

    public Component getHoverName() {
        return getFluidType().getDescription(this);
    }

    public String getDescriptionId() {
        return getFluidType().getDescriptionId(this);
    }

    public static boolean isSameFluid(FluidStack first, FluidStack second) {
        return first.getFluid() == second.getFluid();
    }

    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        if (!isSameFluid(first, second)) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        return Objects.equals(first.components, second.components);
    }

    public static boolean matches(FluidStack first, FluidStack second) {
        if (first == second) return true;
        return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
    }

    public static int hashFluidAndComponents(@Nullable FluidStack stack) {
        if (stack == null) return 0;
        int hash = 31 + stack.getFluid().hashCode();
        return 31 * hash + stack.getComponents().hashCode();
    }

    public void ifNotEmpty(Consumer<FluidStack> consumer) {
        if (!isEmpty()) consumer.accept(this);
    }

    @Override
    public String toString() {
        return getAmount() + " " + BuiltInRegistries.FLUID.getKey(getFluid());
    }
}
