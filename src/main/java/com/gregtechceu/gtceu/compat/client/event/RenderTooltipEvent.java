package com.gregtechceu.gtceu.compat.client.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Either;

import java.util.List;

/**
 * NeoForge's tooltip rendering events. Vanilla tooltips do not post these on Fabric; ModularUI's rich tooltips do,
 * so listeners can still restyle them.
 */
public abstract class RenderTooltipEvent extends Event {

    protected final ItemStack itemStack;
    protected final GuiGraphicsExtractor graphics;
    protected int x;
    protected int y;
    protected Font font;
    protected final List<ClientTooltipComponent> components;

    protected RenderTooltipEvent(ItemStack itemStack, GuiGraphicsExtractor graphics, int x, int y, Font font,
                                 List<ClientTooltipComponent> components) {
        this.itemStack = itemStack;
        this.graphics = graphics;
        this.components = List.copyOf(components);
        this.x = x;
        this.y = y;
        this.font = font;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public GuiGraphicsExtractor getGraphics() {
        return graphics;
    }

    public List<ClientTooltipComponent> getComponents() {
        return components;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Font getFont() {
        return font;
    }

    public static class GatherComponents extends Event implements ICancellableEvent {

        private final ItemStack itemStack;
        private final int screenWidth;
        private final int screenHeight;
        private final List<Either<FormattedText, TooltipComponent>> tooltipElements;
        private int maxWidth;

        public GatherComponents(ItemStack itemStack, int screenWidth, int screenHeight,
                                List<Either<FormattedText, TooltipComponent>> tooltipElements, int maxWidth) {
            this.itemStack = itemStack;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.tooltipElements = tooltipElements;
            this.maxWidth = maxWidth;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getScreenWidth() {
            return screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public List<Either<FormattedText, TooltipComponent>> getTooltipElements() {
            return tooltipElements;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }
    }

    public static class Pre extends RenderTooltipEvent implements ICancellableEvent {

        private final int screenWidth;
        private final int screenHeight;
        private final ClientTooltipPositioner positioner;

        public Pre(ItemStack stack, GuiGraphicsExtractor graphics, int x, int y, int screenWidth, int screenHeight,
                   Font font, List<ClientTooltipComponent> components, ClientTooltipPositioner positioner) {
            super(stack, graphics, x, y, font, components);
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.positioner = positioner;
        }

        public int getScreenWidth() {
            return screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public ClientTooltipPositioner getTooltipPositioner() {
            return positioner;
        }

        public void setFont(Font fr) {
            this.font = fr;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    public static class Color extends RenderTooltipEvent {

        private final int originalBackground;
        private final int originalBorderStart;
        private final int originalBorderEnd;
        private int backgroundStart;
        private int backgroundEnd;
        private int borderStart;
        private int borderEnd;

        public Color(ItemStack stack, GuiGraphicsExtractor graphics, int x, int y, Font fr, int background,
                     int borderStart, int borderEnd, List<ClientTooltipComponent> components) {
            super(stack, graphics, x, y, fr, components);
            this.originalBackground = background;
            this.originalBorderStart = borderStart;
            this.originalBorderEnd = borderEnd;
            this.backgroundStart = background;
            this.backgroundEnd = background;
            this.borderStart = borderStart;
            this.borderEnd = borderEnd;
        }

        public int getBackgroundStart() {
            return backgroundStart;
        }

        public int getBackgroundEnd() {
            return backgroundEnd;
        }

        public void setBackground(int background) {
            setBackgroundStart(background);
            setBackgroundEnd(background);
        }

        public void setBackgroundStart(int backgroundStart) {
            this.backgroundStart = backgroundStart;
        }

        public void setBackgroundEnd(int backgroundEnd) {
            this.backgroundEnd = backgroundEnd;
        }

        public int getBorderStart() {
            return borderStart;
        }

        public void setBorderStart(int borderStart) {
            this.borderStart = borderStart;
        }

        public int getBorderEnd() {
            return borderEnd;
        }

        public void setBorderEnd(int borderEnd) {
            this.borderEnd = borderEnd;
        }

        public int getOriginalBackgroundStart() {
            return originalBackground;
        }

        public int getOriginalBackgroundEnd() {
            return originalBackground;
        }

        public int getOriginalBorderStart() {
            return originalBorderStart;
        }

        public int getOriginalBorderEnd() {
            return originalBorderEnd;
        }
    }
}
