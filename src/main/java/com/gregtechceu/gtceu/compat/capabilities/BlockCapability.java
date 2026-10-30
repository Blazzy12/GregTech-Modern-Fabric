package com.gregtechceu.gtceu.compat.capabilities;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** A block capability backed by a Fabric {@link BlockApiLookup}. */
public final class BlockCapability<T, C> {

    private final Identifier name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;
    private final BlockApiLookup<T, C> lookup;

    private BlockCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
        this.lookup = BlockApiLookup.get(name, typeClass, contextClass);
    }

    public static <T, C> BlockCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new BlockCapability<>(name, typeClass, contextClass);
    }

    public static <T> BlockCapability<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static <T> BlockCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    public Identifier name() {
        return name;
    }

    public Class<T> typeClass() {
        return typeClass;
    }

    public Class<C> contextClass() {
        return contextClass;
    }

    public BlockApiLookup<T, C> lookup() {
        return lookup;
    }

    public @Nullable T getCapability(Level level, BlockPos pos, @Nullable BlockState state,
                                     @Nullable BlockEntity blockEntity, C context) {
        return lookup.find(level, pos, state, blockEntity, context);
    }

    @Override
    public String toString() {
        return "BlockCapability[" + name + "]";
    }
}
