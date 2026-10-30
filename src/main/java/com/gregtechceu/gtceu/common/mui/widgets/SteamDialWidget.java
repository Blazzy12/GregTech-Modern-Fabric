package com.gregtechceu.gtceu.common.mui.widgets;

import brachy.modularui.drawable.MuiGeometry;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.value.sync.DoubleSyncValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Matrix4f;

@Accessors(chain = true)
public class SteamDialWidget implements IDrawable {

    private final DoubleSyncValue progress;
    @Setter
    private float minAngle;
    @Setter
    private float maxAngle;
    @Setter
    private int color;
    private float lastAngle = Float.NaN;
    @Setter
    private UITexture texture;

    public SteamDialWidget(DoubleSyncValue progress) {
        this.progress = progress;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiGraphicsExtractor graphics = context.getGraphics();

        float halfH = height / 2.0f;

        final float progressPercent = Mth.clamp(progress.getFloatValue(), 0.0f, 1.0f);
        final float angle = Mth.lerp(progressPercent, this.minAngle, this.maxAngle);

        if (Float.isNaN(lastAngle)) {
            lastAngle = angle;
        } else {
            lastAngle = (lastAngle + angle) / 2.0f;
        }
        final float lastAngleF = lastAngle;

        final float sinAngle = Mth.sin(-lastAngleF);
        final float cosAngle = Mth.cos(-lastAngleF);

        if (texture == null) {
            MuiGeometry.strip(graphics)
                    .vertex(x + width * cosAngle, y + width * sinAngle).color(color)
                    .vertex(x + halfH * sinAngle, y - halfH * cosAngle).color(color)
                    .vertex(x - halfH * sinAngle, y + halfH * cosAngle).color(color)
                    .vertex(x - halfH * cosAngle, y - halfH * sinAngle).color(color)
                    .submit();
        } else {
            MuiGeometry.strip(graphics, MuiGeometry.texture(texture.location))
                    .vertex(x + width * cosAngle, y + width * sinAngle).uv(0.0f, 0.0f).color(color)
                    .vertex(x + halfH * sinAngle, y - halfH * cosAngle).uv(1.0f, 0.0f).color(color)
                    .vertex(x - halfH * sinAngle, y + halfH * cosAngle).uv(0.0f, 1.0f).color(color)
                    .vertex(x - halfH * cosAngle, y - halfH * sinAngle).uv(1.0f, 1.0f).color(color)
                    .submit();
        }
    }
}
