package com.gregtechceu.gtceu.compat.capabilities;

import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;

/** An entity capability backed by a Fabric {@link EntityApiLookup}. */
public final class EntityCapability<T, C> {

    private final Identifier name;
    private final EntityApiLookup<T, C> lookup;

    private EntityCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.lookup = EntityApiLookup.get(name, typeClass, contextClass);
    }

    public static <T, C> EntityCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new EntityCapability<>(name, typeClass, contextClass);
    }

    public static <T> EntityCapability<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static <T> EntityCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    public Identifier name() {
        return name;
    }

    public EntityApiLookup<T, C> lookup() {
        return lookup;
    }

    public @Nullable T getCapability(Entity entity, C context) {
        return lookup.find(entity, context);
    }
}
