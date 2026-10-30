package brachy.modularui.core.mixins.client;

import brachy.modularui.client.ModularUIClient;
import brachy.modularui.screen.ClientScreenHandler;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceGameTime(J)I",
                     shift = At.Shift.AFTER))
    public void modularui$updateTimer(CallbackInfo ci) {
        int ticks = ModularUIClient.getTimer60Fps().advanceGameTime(Util.getMillis());
        for (int j = 0; j < Math.min(20, ticks); ++j) {
            ClientScreenHandler.onFrameUpdate();
        }
    }
}
