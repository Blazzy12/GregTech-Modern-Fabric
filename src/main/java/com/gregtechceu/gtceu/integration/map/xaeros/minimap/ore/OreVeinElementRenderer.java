package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.integration.map.xaeros.common.ore.OreVeinElement;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.data.AtlasIds;

import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.lib.client.graphics.XaeroBufferProvider;
import com.gregtechceu.gtceu.integration.map.xaeros.common.XaeroDraw;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;

public class OreVeinElementRenderer extends MinimapElementRenderer<OreVeinElement, OreVeinElementContext> {

    private OreVeinElementRenderer(OreVeinElementReader elementReader,
                                   OreVeinElementRenderProvider provider,
                                   OreVeinElementContext context) {
        super(elementReader, provider, context);
    }

    @Override
    public void preRender(MinimapElementRenderInfo renderInfo, XaeroBufferProvider buffers,
                          MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean renderElement(OreVeinElement element, boolean highlit, boolean outOfBounds,
                                 double optionalDepth, float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo, MinimapElementGraphics graphics,
                                 XaeroBufferProvider buffers) {
        GeneratedVeinMetadata vein = element.getVein();
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        Material material = OreRenderLayer.getMaterial(vein);
        int materialARGB = material == null ? 0xFFFFFFFF : material.getMaterialARGB();

        Identifier oreTexture = MaterialIconType.rawOre
                .getItemTexturePath(material == null ? MaterialIconSet.DULL.value() : material.getMaterialIconSet(),
                        true);
        if (oreTexture != null) {
            var oreSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS)
                    .getSprite(oreTexture);
            XaeroDraw.tintedSprite(graphics.pose(), buffers, oreSprite, -iconSize / 2, -iconSize / 2, iconSize,
                    iconSize, materialARGB | 0xFF000000);
        }

        int borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(materialARGB | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            int thickness = Math.max(1, iconSize / 16);
            int half = iconSize / 2;
            graphics.fill(-half, -half, half, -half + thickness, borderColor);
            graphics.fill(-half, half - thickness, half, half, borderColor);
            graphics.fill(-half, -half, -half + thickness, half, borderColor);
            graphics.fill(half - thickness, -half, half, half, borderColor);
        }
        return true;
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, XaeroBufferProvider buffers,
                           MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean shouldRender(MinimapElementRenderLocation location) {
        return GroupingMapRenderer.getInstance().doShowLayer("ore_veins") &&
                location == MinimapElementRenderLocation.IN_MINIMAP;
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementReader(), new OreVeinElementRenderProvider(),
                    new OreVeinElementContext());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
