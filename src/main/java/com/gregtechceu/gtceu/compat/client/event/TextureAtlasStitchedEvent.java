package com.gregtechceu.gtceu.compat.client.event;

import com.gregtechceu.gtceu.compat.bus.Event;

import net.minecraft.client.renderer.texture.TextureAtlas;

/** Fired by GregTech once an atlas is ready after a resource reload. */
public class TextureAtlasStitchedEvent extends Event {

    private final TextureAtlas atlas;

    public TextureAtlasStitchedEvent(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}
