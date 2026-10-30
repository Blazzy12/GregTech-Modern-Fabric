package brachy.modularui.core.mixins.client;

import brachy.modularui.screen.ClientScreenHandler;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks screen changes for {@link ClientScreenHandler} (NeoForge's {@code ScreenEvent.Opening} before). The current
 * screen lives on {@link Gui} in 26.x.
 */
@Mixin(Gui.class)
public class GuiScreenMixin {

    @Shadow
    private @Nullable Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void modularui$trackRealScreenClose(@Nullable Screen newScreen, CallbackInfo ci) {
        if (newScreen == null && this.screen != null) {
            // only when the next screen is null do all screens close (a transition is not a close)
            ClientScreenHandler.onCloseScreens(this.screen);
        }
    }

    @Inject(method = "setScreen",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;",
                     opcode = org.objectweb.asm.Opcodes.PUTFIELD))
    private void modularui$onOpenScreen(@Nullable Screen arg, CallbackInfo ci, @Local(argsOnly = true) Screen newScreen) {
        ClientScreenHandler.onOpenScreen(this.screen, newScreen);
    }
}
