package com.gregtechceu.gtceu.client.renderer.item.decorator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import com.gregtechceu.gtceu.compat.client.IItemDecorator;

import brachy.modularui.drawable.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GTLampItemOverlayRenderer implements IItemDecorator {

    public static final GTLampItemOverlayRenderer INSTANCE = new GTLampItemOverlayRenderer();

    private GTLampItemOverlayRenderer() {}

    @Override
    public boolean render(@NotNull GuiGraphicsExtractor graphics, @NotNull Font font,
                          ItemStack stack, int xPosition, int yPosition) {
        LampBlockItem.LampData lampData = stack.get(GTDataComponents.LAMP_DATA);
        if (lampData == null) {
            return false;
        }
        if (lampData.lit() && lampData.bloom()) {
            return false;
        }

        if (!lampData.bloom()) {
            var texture = GTGuiTextures.LAMP_NO_BLOOM;
            GuiDraw.drawTexture(graphics, texture.location, xPosition, yPosition, xPosition + 16,
                    yPosition + 16, texture.u0, texture.v0,
                    texture.u1, texture.v1);
        }

        if (!lampData.lit()) {
            var texture = GTGuiTextures.LAMP_NO_LIGHT;
            GuiDraw.drawTexture(graphics, texture.location, xPosition, yPosition, xPosition + 16,
                    yPosition + 16, texture.u0, texture.v0,
                    texture.u1, texture.v1);
        }
        return true;
    }
}
