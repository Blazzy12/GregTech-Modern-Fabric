package com.gregtechceu.gtceu.core.mixins.fabric;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** GT's saved data has no vanilla data fixes; skip fixing when a type declares none. */
@Mixin(SavedDataStorage.class)
public class SavedDataStorageMixin {

    @WrapOperation(method = "readTagFromDisk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag gtceu$skipNullFix(DataFixTypes type, DataFixer fixer, CompoundTag tag, int version,
                                          int newVersion, Operation<CompoundTag> original) {
        return type == null ? tag : original.call(type, fixer, tag, version, newVersion);
    }
}
