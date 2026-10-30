package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.gui;

import net.minecraft.resources.Identifier;

import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.GuiTexturedButton;

import java.util.function.Supplier;

/** A textured button whose texture is {@code spriteW} x {@code spriteH} (Xaero's button takes this directly now). */
public class GuiTexturedButtonWithSize extends GuiTexturedButton {

    public GuiTexturedButtonWithSize(int x, int y, int w, int h, int textureX, int textureY, int textureW, int textureH,
                                     int spriteW, int spriteH, Identifier texture, OnPress onPress,
                                     Supplier<Tooltip> tooltip) {
        super(x, y, w, h, textureX, textureY, textureW, textureH, texture, onPress, tooltip, spriteW, spriteH);
    }
}
