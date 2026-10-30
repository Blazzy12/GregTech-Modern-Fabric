package com.gregtechceu.gtceu.integration.map.xaeros.common;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import xaero.lib.client.graphics.XaeroBufferProvider;

/** Drawing Xaero's element graphics cannot do itself: atlas sprites tinted with a colour. */
public final class XaeroDraw {

    private static final int FULL_BRIGHT = 0xF000F0;

    private XaeroDraw() {}

    public static void tintedSprite(PoseStack pose, XaeroBufferProvider buffers, TextureAtlasSprite sprite,
                                    int x, int y, int width, int height, int argb) {
        VertexConsumer buffer = buffers.getBuffer(RenderTypes.text(sprite.atlasLocation()));
        Matrix4f matrix = pose.last().pose();
        buffer.addVertex(matrix, x, y + height, 0).setColor(argb).setUv(sprite.getU0(), sprite.getV1())
                .setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, x + width, y + height, 0).setColor(argb).setUv(sprite.getU1(), sprite.getV1())
                .setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, x + width, y, 0).setColor(argb).setUv(sprite.getU1(), sprite.getV0())
                .setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, x, y, 0).setColor(argb).setUv(sprite.getU0(), sprite.getV0())
                .setLight(FULL_BRIGHT);
    }
}
