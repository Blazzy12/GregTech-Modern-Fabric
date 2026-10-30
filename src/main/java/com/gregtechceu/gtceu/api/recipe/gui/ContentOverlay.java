package com.gregtechceu.gtceu.api.recipe.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.compat.client.gui.TextColors;

import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import com.gregtechceu.gtceu.compat.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.mojang.blaze3d.systems.RenderSystem;

@Environment(EnvType.CLIENT)
public record ContentOverlay(Content content, boolean perTick)
        implements IDrawable {

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawChance(context.getGraphics(), x, y, width, height);
        drawRangeAmount(context.getGraphics(), x, y, width, height);
        drawFluidAmount(context.getGraphics(), x, y, width, height);
        if (perTick) {
            drawTick(context.getGraphics(), x, y, width, height);
        }
    }

    public void drawRangeAmount(GuiGraphicsExtractor graphics, float x, float y, int width, int height) {
        if (content.content() instanceof IntProviderIngredient ingredient) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, 0);
            graphics.pose().scale(0.5f, 0.5f);
            int min = ingredient.getCountProvider().minInclusive();
            int max = ingredient.getCountProvider().maxInclusive();
            String s = String.format("%s-%s", min, max);
            int color = 0xFFFFFF;
            Font fontRenderer = Minecraft.getInstance().font;
            // 5 == max num of characters that fit in a slot at 0.5x render size
            if (s.length() > 5) {
                s = "X-Y";
                color = net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.GOLD).getValue(); // Orange?
            }
            graphics.text(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 21),
                    (int) ((y + (height / 3f) + 6) * 2), TextColors.legacy(color), true);
            graphics.pose().popMatrix();
        }
    }

    public void drawFluidAmount(GuiGraphicsExtractor graphics, float x, float y, int width, int height) {
        if (content.content() instanceof SizedFluidIngredient ingredient) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, 0);
            graphics.pose().scale(0.5f, 0.5f);
            Font fontRenderer = Minecraft.getInstance().font;
            int color;
            String s;
            if (ingredient.ingredient() instanceof IRangedIngredient) {
                // with only 5 characters worth of space, that's not enough for a fluid range
                color = net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.GOLD).getValue();
                s = "X-Y";
            } else {
                int amount = ingredient.amount();
                color = 0xFFFFFF;
                s = FormattingUtil.formatBuckets(amount);
                if (fontRenderer.width(s) > 32)
                    s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_1F, "B");
                if (fontRenderer.width(s) > 32)
                    s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_0F, "B");
            }
            graphics.text(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 22),
                    (int) ((y + (height / 3f) + 6) * 2), TextColors.legacy(color), true);
            graphics.pose().popMatrix();
        }
    }

    public void drawChance(GuiGraphicsExtractor graphics, float x, float y, int width, int height) {
        if (content.chance() == ChanceLogic.getMaxChancedValue()) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        graphics.pose().scale(0.5f, 0.5f);
        float chanceFloat = 1f * content.chance() / content.maxChance();
        String percent = FormattingUtil.formatNumber2Places(100 * chanceFloat);

        Component s = content.chance() == 0 ? Component.translatable("gtceu.gui.content.chance_nc_short") :
                Component.literal(percent + "%");

        int color = content.chance() == 0 ? 0xFF0000 : GradientUtil.toRGB(Mth.lerp(chanceFloat, 29f, 167f), 100f, 50f);
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.text(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height), TextColors.legacy(color), true);
        graphics.pose().popMatrix();
    }

    public void drawTick(GuiGraphicsExtractor graphics, float x, float y, int width, int height) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        graphics.pose().scale(0.5f, 0.5f);

        Component s = Component.translatable("gtceu.gui.content.tips.per_tick_short");

        int color = 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.text(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height +
                        (content.chance() == ChanceLogic.getMaxChancedValue() ? 0 : 10)),
                TextColors.legacy(color));
        graphics.pose().popMatrix();
    }
}
