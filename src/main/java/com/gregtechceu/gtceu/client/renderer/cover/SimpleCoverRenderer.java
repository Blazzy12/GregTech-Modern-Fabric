package com.gregtechceu.gtceu.client.renderer.cover;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;

import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleCoverRenderer implements ICoverRenderer {

    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite sprite = null;
    @Environment(EnvType.CLIENT)
    protected TextureAtlasSprite emissiveSprite = null;

    public SimpleCoverRenderer(Identifier texture) {
        this(texture, null);
    }

    public SimpleCoverRenderer(Identifier texture, Identifier emissiveTexture) {
        ModelEventHelper.registerAtlasStitchedEventListener(false, TextureAtlas.LOCATION_BLOCKS, event -> {
            var atlas = event.getAtlas();

            sprite = atlas.getSprite(texture);
            if (emissiveTexture != null) {
                emissiveSprite = atlas.getSprite(emissiveTexture);
            } else {
                Identifier emissiveTex = texture.withSuffix("_emissive");
                if (atlas.getSprite(emissiveTex) != atlas.missingSprite()) {
                    emissiveSprite = atlas.getSprite(emissiveTex);
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable ChunkSectionLayer renderType) {
        if (side == null || side == coverBehavior.attachedSide) {
            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide, sprite)
                    .gtceu$setFaceLayer(FaceLayer.COVER));
            if (emissiveSprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveSprite).gtceu$setFaceLayer(FaceLayer.COVER_EMISSIVE));
            }
        }
    }
}
