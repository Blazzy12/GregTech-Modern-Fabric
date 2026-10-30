package com.gregtechceu.gtceu.client.bloom;


import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * How a group of bloom effects or particles is drawn. 26.x draws through render types instead of GL state set
 * around a buffer.
 */
public interface IRenderSetup {

    RenderType getRenderType();
}
