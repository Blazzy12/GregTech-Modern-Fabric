package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.client.TooltipsHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.client.fluid.ClientFluidStackHooks;
import dev.architectury.hooks.fluid.FluidStackHooks;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws fluid entries in REI. REI 26.3's own fluid renderer looks up the sprite and colour and then draws nothing, so
 * no fluid showed in its list or recipes; this one draws the tinted still sprite.
 */
public record ReiFluidRenderer(EntryRenderer<FluidStack> original) implements EntryRenderer<FluidStack> {

    @Override
    public void render(EntryStack<FluidStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY,
                       float delta) {
        FluidStack stack = entry.getValue();
        if (stack.isEmpty()) return;
        var sprite = ClientFluidStackHooks.getStillTexture(stack);
        if (sprite == null) return;
        int color = ClientFluidStackHooks.getColor(stack);
        if ((color >>> 24) == 0) color |= 0xFF000000;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, bounds.x, bounds.y, bounds.width, bounds.height,
                color);
    }

    /** The same tooltip as in JEI: name, GT's fluid info (formula, state, temperature, ...) and the amount in mB. */
    @Override
    public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        FluidStack stack = entry.getValue();
        if (stack.isEmpty()) return original.getTooltip(entry, context);
        long millibuckets = stack.getAmount() / (FluidStackHooks.bucketAmount() / 1000);
        var gtStack = new com.gregtechceu.gtceu.compat.fluids.FluidStack(stack.getFluid(),
                (int) Math.min(Integer.MAX_VALUE, millibuckets), stack.getPatch());
        List<Component> lines = new ArrayList<>();
        lines.add(FluidStackHooks.getName(stack));
        TooltipsHandler.appendFluidTooltips(gtStack, lines::add, context.getFlag(), context.vanillaContext());
        lines.add(Component.literal(String.format("%,d mB", millibuckets)).withStyle(ChatFormatting.GRAY));
        return Tooltip.create(context.getPoint(), lines);
    }
}
