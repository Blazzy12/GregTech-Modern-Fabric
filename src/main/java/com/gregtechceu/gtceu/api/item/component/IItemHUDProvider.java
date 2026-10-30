package com.gregtechceu.gtceu.api.item.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * Provides a drawable HUD for the item
 */
public interface IItemHUDProvider extends IItemComponent {

    /**
     * @return if the HUD needs to be drawn
     */
    @Environment(EnvType.CLIENT)
    default boolean shouldDrawHUD() {
        return true;
    }

    /**
     * Draws the HUD
     *
     * @param stack the ItemStack to retrieve information from
     */
    @Environment(EnvType.CLIENT)
    default void drawHUD(ItemStack stack, ArmorUtils.ModularHUD hud, GuiGraphicsExtractor guiGraphics) {}

    /**
     * Checks and draws the hud for a provider
     *
     * @param provider the provider whose hud to draw
     * @param hud      Hud helper for rendering strings
     * @param stack    the stack the provider should use
     */
    @Environment(EnvType.CLIENT)
    static void tryDrawHud(@NotNull IItemHUDProvider provider, @NotNull ItemStack stack, ArmorUtils.ModularHUD hud,
                           GuiGraphicsExtractor guiGraphics) {
        if (provider.shouldDrawHUD()) provider.drawHUD(stack, hud, guiGraphics);
    }
}
