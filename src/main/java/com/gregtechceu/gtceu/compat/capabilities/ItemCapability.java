package com.gregtechceu.gtceu.compat.capabilities;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** An item capability backed by a Fabric {@link ItemApiLookup}. */
public final class ItemCapability<T, C> {

    private final Identifier name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;
    private final ItemApiLookup<T, C> lookup;

    private ItemCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
        this.lookup = ItemApiLookup.get(name, typeClass, contextClass);
    }

    public static <T, C> ItemCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new ItemCapability<>(name, typeClass, contextClass);
    }

    public static <T> ItemCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
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

    public ItemApiLookup<T, C> lookup() {
        return lookup;
    }

    public @Nullable T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) return null;
        return lookup.find(stack, context);
    }

    @Override
    public String toString() {
        return "ItemCapability[" + name + "]";
    }
}
