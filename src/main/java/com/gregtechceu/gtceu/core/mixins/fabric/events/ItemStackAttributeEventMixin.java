package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.ItemAttributeModifierEvent;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

/** Adds the attribute modifiers listeners give a stack (GregTech armor and tool modules). */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributeEventMixin {

    private ItemAttributeModifiers gtceu$extraModifiers() {
        if (((ItemStack) (Object) this).isEmpty()) return ItemAttributeModifiers.EMPTY;
        return NeoForge.EVENT_BUS.post(new ItemAttributeModifierEvent((ItemStack) (Object) this,
                ItemAttributeModifiers.EMPTY)).getModifiers();
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
            at = @At("TAIL"))
    private void gtceu$extraModifiers(EquipmentSlotGroup group,
                                      TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer,
                                      CallbackInfo ci) {
        gtceu$extraModifiers().forEach(group, consumer);
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("TAIL"))
    private void gtceu$extraModifiers(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
                                      CallbackInfo ci) {
        gtceu$extraModifiers().forEach(slot, consumer);
    }
}
