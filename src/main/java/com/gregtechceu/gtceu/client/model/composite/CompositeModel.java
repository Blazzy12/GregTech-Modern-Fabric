package com.gregtechceu.gtceu.client.model.composite;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import com.gregtechceu.gtceu.compat.client.model.ChunkRenderTypeSet;
import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import com.gregtechceu.gtceu.compat.client.model.VanillaUnbakedModel;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryBakingContext;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryLoader;
import com.gregtechceu.gtceu.compat.client.model.geometry.IUnbakedGeometry;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A model made of several child models, like NeoForge's {@code neoforge:composite} (which ore models use, a base
 * stone and an ore overlay). Written as {@code "fabric:type": "gtceu:composite"} with a {@code "children"} object.
 */
public class CompositeModel implements IUnbakedGeometry<CompositeModel> {

    public static final Identifier ID = GTCEu.id("composite");

    private final Map<String, VanillaUnbakedModel> children;
    private final Set<String> hidden;

    public CompositeModel(Map<String, VanillaUnbakedModel> children, Set<String> hidden) {
        this.children = children;
        this.hidden = hidden;
    }

    @Override
    public Collection<Identifier> getDependencies() {
        Set<Identifier> dependencies = new HashSet<>();
        for (VanillaUnbakedModel child : children.values()) {
            dependencies.addAll(child.getDependencies());
        }
        return dependencies;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        List<BakedModel> baked = new ArrayList<>();
        for (var entry : children.entrySet()) {
            if (hidden.contains(entry.getKey())) continue;
            baked.add(entry.getValue().bake(baker, spriteGetter, modelState));
        }
        TextureAtlasSprite particle = context.hasMaterial("particle") ?
                spriteGetter.apply(context.getMaterial("particle")) :
                baked.isEmpty() ? BakedModel.missingSprite() : baked.getFirst().getParticleIcon();
        return new Baked(baked, particle, context.getTransforms(), context.useAmbientOcclusion());
    }

    public record Baked(List<BakedModel> children, TextureAtlasSprite particle, ItemTransforms transforms,
                        boolean ambientOcclusion)
            implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                        ModelData data, @Nullable ChunkSectionLayer renderType) {
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedModel child : children) {
                quads.addAll(child.getQuads(state, side, rand, data, renderType));
            }
            return quads;
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
            List<ChunkRenderTypeSet> sets = new ArrayList<>();
            for (BakedModel child : children) sets.add(child.getRenderTypes(state, rand, data));
            return ChunkRenderTypeSet.union(sets);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return ambientOcclusion;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }
    }

    public static final class Loader implements IGeometryLoader<CompositeModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public CompositeModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
            Map<String, VanillaUnbakedModel> children = new LinkedHashMap<>();
            for (var entry : GsonHelper.getAsJsonObject(json, "children").entrySet()) {
                children.put(entry.getKey(), VanillaUnbakedModel.parseInline(entry.getValue()));
            }
            Set<String> hidden = new HashSet<>();
            JsonObject visibility = GsonHelper.getAsJsonObject(json, "visibility", null);
            if (visibility != null) {
                for (var entry : visibility.entrySet()) {
                    if (!entry.getValue().getAsBoolean()) hidden.add(entry.getKey());
                }
            }
            return new CompositeModel(children, hidden);
        }
    }
}
