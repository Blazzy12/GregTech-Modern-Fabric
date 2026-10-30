package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.geometry.GeometryLoaderModel;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A vanilla JSON model, by id or inline, baked into a {@link SimpleBakedModel}. If the id names a model with a
 * GregTech loader, that model's own baked form is used instead.
 */
public final class VanillaUnbakedModel implements UnbakedModel {

    private final @Nullable Identifier id;
    private final net.minecraft.client.resources.model.@Nullable UnbakedModel inline;

    private VanillaUnbakedModel(@Nullable Identifier id,
                                net.minecraft.client.resources.model.@Nullable UnbakedModel inline) {
        this.id = id;
        this.inline = inline;
    }

    public static VanillaUnbakedModel of(Identifier id) {
        return new VanillaUnbakedModel(id, null);
    }

    public static VanillaUnbakedModel inline(net.minecraft.client.resources.model.UnbakedModel model) {
        return new VanillaUnbakedModel(null, model);
    }

    /** Parses a model object written inline in another model's JSON. */
    public static VanillaUnbakedModel parseInline(JsonElement json) throws JsonParseException {
        return inline(UnbakedModelDeserializer.deserialize(new StringReader(json.toString())));
    }

    public @Nullable Identifier id() {
        return id;
    }

    @Override
    public Collection<Identifier> getDependencies() {
        if (id != null) return List.of(id);
        Identifier parent = inline.parent();
        return parent != null ? List.of(parent) : List.of();
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver) {}

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        ResolvedModel resolved;
        if (id != null) {
            resolved = baker.getModel(id);
            if (resolved.wrapped() instanceof GeometryLoaderModel custom) {
                return custom.bake(resolved, baker, state);
            }
        } else {
            Identifier parent = inline.parent();
            resolved = new InlineResolvedModel(inline, parent == null ? null : baker.getModel(parent),
                    "inline model" + (parent == null ? "" : " of " + parent));
        }
        return bakeVanilla(resolved, baker, state);
    }

    /** Bakes a resolved vanilla model, keeping the texture keys and face layers of its quads. */
    public static SimpleBakedModel bakeVanilla(ResolvedModel resolved, ModelBaker baker, ModelState state) {
        TextureSlots slots = resolved.getTopTextureSlots();
        Map<net.minecraft.client.resources.model.geometry.BakedQuad, CuboidFace> faces = QuadMetadata.newCapture();
        QuadCollection quads = QuadMetadata.capture(() -> resolved.bakeTopGeometry(slots, baker, state), faces);
        TextureAtlasSprite particle = resolved.resolveParticleMaterial(slots, baker).sprite();
        return SimpleBakedModel.of(quads, faces, resolved.getTopAmbientOcclusion(), particle,
                resolved.getTopTransforms());
    }

    /** @return a sprite getter backed by the baker's materials */
    public static Function<Material, TextureAtlasSprite> spriteGetter(ModelBaker baker, String modelName) {
        return material -> baker.materials().get(material, () -> modelName).sprite();
    }
}
