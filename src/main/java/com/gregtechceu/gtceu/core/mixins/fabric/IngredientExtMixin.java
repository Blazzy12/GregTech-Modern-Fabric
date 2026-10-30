package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.IngredientExt;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the injected {@link IngredientExt} interface present at runtime, and keeps 1.21.1's rule that a tag
 * ingredient is never empty: GT builds its recipes while data loads, before tags can be read.
 */
@Mixin(Ingredient.class)
public abstract class IngredientExtMixin implements IngredientExt {

    @Shadow
    @Final
    public HolderSet<Item> values;

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void gtceu$tagsAreNotEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (this.values instanceof HolderSet.Named<Item>) {
            cir.setReturnValue(false);
        }
    }
}
