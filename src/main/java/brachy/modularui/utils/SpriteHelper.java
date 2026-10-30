package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpriteHelper {

    public static TextureAtlasSprite getSpriteOfBlockState(BlockState blockState, Direction facing) {
        List<BakedQuad> quads = getQuadsOfBlockState(blockState, facing);
        if (quads.isEmpty()) {
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            return model.particleMaterial().sprite();
        }
        return quads.getFirst().materialInfo().sprite();
    }

    public static List<BakedQuad> getQuadsOfBlockState(BlockState blockState, @Nullable Direction facing) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            quads.addAll(part.getQuads(facing));
        }
        return quads;
    }

    public static TextureAtlasSprite getSpriteOfItem(ItemStack item) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, item, ItemDisplayContext.GUI, null,
                null, 0);
        Material.Baked material = state.pickParticleMaterial(RandomSource.create());
        return material != null ? material.sprite() : missingSprite();
    }

    private static TextureAtlasSprite missingSprite() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }
}
