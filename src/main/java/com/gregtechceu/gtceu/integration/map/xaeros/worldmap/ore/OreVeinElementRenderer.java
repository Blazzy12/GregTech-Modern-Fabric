package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.integration.map.xaeros.common.XaeroDraw;
import com.gregtechceu.gtceu.integration.map.xaeros.common.ore.OreVeinElement;

import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.map.element.MapElementGraphics;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;

public class OreVeinElementRenderer extends
                                    ElementRenderer<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> {

    protected OreVeinElementRenderer(OreVeinElementContext context,
                                     ElementRenderProvider<OreVeinElement, OreVeinElementContext> provider,
                                     ElementReader<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> reader) {
        super(context, provider, reader);
    }

    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }

    @Override
    public void preRender(ElementRenderInfo info, XaeroBufferProvider buffers,
                          MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {}

    @Override
    public void postRender(ElementRenderInfo info, XaeroBufferProvider buffers,
                           MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {}

    @Override
    public void renderElementShadow(OreVeinElement element, boolean hovered, float optionalScale, double partialX,
                                    double partialY, ElementRenderInfo info, MapElementGraphics graphics,
                                    XaeroBufferProvider buffers,
                                    MultiTextureRenderTypeRendererProvider rendererProvider) {}

    @Override
    public boolean renderElement(OreVeinElement element, boolean hovered, double optionalDepth, float optionalScale,
                                 double partialX, double partialY, ElementRenderInfo info,
                                 MapElementGraphics graphics, XaeroBufferProvider buffers,
                                 MultiTextureRenderTypeRendererProvider rendererProvider) {
        GeneratedVeinMetadata vein = element.getVein();
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        Material material = OreRenderLayer.getMaterial(vein);
        if (material == null) return false;
        int materialARGB = material.getMaterialARGB();

        drawLayer(graphics, buffers, MaterialIconType.rawOre.getItemTexturePath(material.getMaterialIconSet(), true),
                iconSize, materialARGB);
        drawLayer(graphics, buffers,
                MaterialIconType.rawOre.getItemTexturePath(material.getMaterialIconSet(), "secondary", true),
                iconSize, material.getMaterialSecondaryARGB());

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

    private static void drawLayer(MapElementGraphics graphics, XaeroBufferProvider buffers,
                                  Identifier texture, int iconSize, int argb) {
        if (texture == null) return;
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(texture);
        XaeroDraw.tintedSprite(graphics.pose(), buffers, sprite, -iconSize / 2, -iconSize / 2, iconSize, iconSize,
                argb | 0xFF000000);
    }

    @Override
    public boolean shouldRender(ElementRenderLocation location, boolean shadow) {
        return GroupingMapRenderer.getInstance().doShowLayer("ore_veins");
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementContext(), new OreVeinElementRenderProvider(),
                    new OreVeinElementReader());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
