package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Lets listeners add attribute modifiers to a stack when its modifiers are queried. */
public class ItemAttributeModifierEvent extends Event {

    private final ItemStack itemStack;
    private ItemAttributeModifiers modifiers;

    public ItemAttributeModifierEvent(ItemStack itemStack, ItemAttributeModifiers modifiers) {
        this.itemStack = itemStack;
        this.modifiers = modifiers;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemAttributeModifiers getModifiers() {
        return modifiers;
    }

    public void addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        modifiers = modifiers.withModifierAdded(attribute, modifier, slot);
    }
}
