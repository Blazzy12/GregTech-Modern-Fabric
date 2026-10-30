package com.gregtechceu.gtceu.client.renderer.cover;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IIOCover;
import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;

import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IOCoverRenderer implements ICoverRenderer {

    public static final IOCoverRenderer PUMP_LIKE_COVER_RENDERER = new IOCoverRenderer(
            GTCEu.id("block/cover/pump"),
            GTCEu.id("block/cover/pump_inverted"),
            null, null);

    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite overlaySprite = null;
    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite invertedOverlaySprite = null;
    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite emissiveOverlaySprite = null;
    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite invertedEmissiveOverlaySprite = null;

    public IOCoverRenderer(@Nullable Identifier overlay,
                           @Nullable Identifier invertedOverlay,
                           @Nullable Identifier emissiveOverlay,
                           @Nullable Identifier invertedEmissiveOverlay) {
        ModelEventHelper.registerAtlasStitchedEventListener(false, TextureAtlas.LOCATION_BLOCKS, event -> {
            var atlas = event.getAtlas();

            if (overlay != null) {
                overlaySprite = atlas.getSprite(overlay);
            }
            if (invertedOverlay != null) {
                invertedOverlaySprite = atlas.getSprite(invertedOverlay);
            }
            if (emissiveOverlay != null) {
                emissiveOverlaySprite = atlas.getSprite(emissiveOverlay);
            }
            if (invertedEmissiveOverlay != null) {
                invertedEmissiveOverlaySprite = atlas.getSprite(invertedEmissiveOverlay);
            }
        });
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            ModelData modelData, @Nullable ChunkSectionLayer renderType) {
        if ((side == null || side == coverBehavior.attachedSide) && coverBehavior instanceof IIOCover ioCover) {
            boolean isInverted = ioCover.getIo() != IO.OUT;

            if (isInverted && invertedOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedOverlaySprite).gtceu$setFaceLayer(FaceLayer.COVER));
            } else if (overlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        overlaySprite).gtceu$setFaceLayer(FaceLayer.COVER));
            }
            if (isInverted && invertedEmissiveOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedEmissiveOverlaySprite, BlockModelRotation.IDENTITY, -101, 15, true, false)
                        .gtceu$setFaceLayer(FaceLayer.COVER_EMISSIVE));
            } else if (emissiveOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveOverlaySprite, BlockModelRotation.IDENTITY, -101, 15, true, false)
                        .gtceu$setFaceLayer(FaceLayer.COVER_EMISSIVE));
            }
        }
    }
}
