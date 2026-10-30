package com.gregtechceu.gtceu.compat.client.model.geometry;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import com.gregtechceu.gtceu.compat.client.model.InlineResolvedModel;
import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;
import com.gregtechceu.gtceu.compat.client.model.VanillaUnbakedModel;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;

/**
 * The vanilla form of a model with a GregTech loader. The plain JSON fields (parent, textures, display, ...) are read
 * as a vanilla model; the rest by the loader.
 * <p>
 * Blockstate variants pointing at it bake to a dynamic {@code GTBlockStateModel} (see the {@code SingleVariant}
 * mixin); everything else that wants vanilla geometry (item models) gets the static quads of {@link #bake}.
 */
public final class GeometryLoaderModel implements net.minecraft.client.resources.model.UnbakedModel {

    private final IUnbakedGeometry<?> customGeometry;
    private final net.minecraft.client.resources.model.UnbakedModel base;

    private GeometryLoaderModel(IUnbakedGeometry<?> customGeometry,
                                net.minecraft.client.resources.model.UnbakedModel base) {
        this.customGeometry = customGeometry;
        this.base = base;
    }

    public IUnbakedGeometry<?> customGeometry() {
        return customGeometry;
    }

    /** Registers a loader under its {@code "fabric:type"} id. */
    public static void register(Identifier id, IGeometryLoader<?> loader) {
        UnbakedModelDeserializer.register(id, (json, context) -> {
            IUnbakedGeometry<?> geometry = loader.read(json, context);
            JsonObject plain = json.deepCopy();
            plain.remove("fabric:type");
            addParticle(plain, json);
            var base = UnbakedModelDeserializer.deserialize(new StringReader(plain.toString()));
            if (geometry == null) return base;
            return new GeometryLoaderModel(geometry, base);
        });
    }

    /**
     * 26.x checks every model for a particle texture. Loader models usually have their textures in nested models
     * (machine variants, pipe parts), so one of those is used, preferring a particle, side or all texture.
     */
    private static void addParticle(JsonObject plain, JsonObject json) {
        JsonObject textures = plain.has("textures") && plain.get("textures").isJsonObject() ?
                plain.getAsJsonObject("textures") : new JsonObject();
        if (textures.has("particle")) return;
        String particle = null;
        for (String key : new String[] { "particle", "side", "all", null }) {
            particle = findTexture(json, key);
            if (particle != null) break;
        }
        if (particle == null) particle = findReferencedTexture(json, 0);
        if (particle == null) return;
        textures.addProperty("particle", particle);
        plain.add("textures", textures);
    }

    /** @return a literal texture of a model the JSON refers to by id ({@code "model"} or {@code "parent"}) */
    private static @Nullable String findReferencedTexture(com.google.gson.JsonElement element, int depth) {
        if (depth > 4) return null;
        java.util.List<String> ids = new java.util.ArrayList<>();
        collectModelIds(element, ids);
        var resources = net.minecraft.client.Minecraft.getInstance().getResourceManager();
        for (String id : ids) {
            Identifier location = Identifier.parse(id).withPath(path -> "models/" + path + ".json");
            var resource = resources.getResource(location);
            if (resource.isEmpty()) continue;
            try (var reader = resource.get().openAsReader()) {
                JsonObject model = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                for (String key : new String[] { "particle", "side", "all", null }) {
                    String found = findTexture(model, key);
                    if (found != null) return found;
                }
                String found = findReferencedTexture(model, depth + 1);
                if (found != null) return found;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static void collectModelIds(com.google.gson.JsonElement element, java.util.List<String> ids) {
        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) collectModelIds(child, ids);
        } else if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                if ((entry.getKey().equals("model") || entry.getKey().equals("parent")) &&
                        entry.getValue().isJsonPrimitive() &&
                        !entry.getValue().getAsString().startsWith("minecraft:block/block")) {
                    ids.add(entry.getValue().getAsString());
                } else {
                    collectModelIds(entry.getValue(), ids);
                }
            }
        }
    }

    /** @return a literal texture under {@code key} (any key if null) in some {@code textures} object of the JSON */
    private static @Nullable String findTexture(com.google.gson.JsonElement element, @Nullable String key) {
        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) {
                String found = findTexture(child, key);
                if (found != null) return found;
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("textures") && object.get("textures").isJsonObject()) {
                for (var entry : object.getAsJsonObject("textures").entrySet()) {
                    if ((key == null || entry.getKey().equals(key)) && entry.getValue().isJsonPrimitive() &&
                            !entry.getValue().getAsString().startsWith("#")) {
                        return entry.getValue().getAsString();
                    }
                }
            }
            for (var entry : object.entrySet()) {
                String found = findTexture(entry.getValue(), key);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Marks the models the geometry refers to, so the model discovery loads them. */
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for (Identifier dependency : customGeometry.getDependencies()) {
            resolver.markDependency(dependency);
        }
    }

    public BakedModel bake(ResolvedModel resolved, ModelBaker baker, ModelState state) {
        TextureSlots slots = resolved.getTopTextureSlots();
        IGeometryBakingContext context = new Context(resolved, slots,
                new InlineResolvedModel(base, resolved.parent(), resolved.debugName()));
        customGeometry.resolveParents(id -> resolve(baker, id), context);
        return customGeometry.bake(context, baker, VanillaUnbakedModel.spriteGetter(baker, resolved.debugName()),
                state, ItemOverrides.EMPTY);
    }

    /** @return the GregTech form of a model by id */
    public static UnbakedModel resolve(ModelBaker baker, Identifier id) {
        return VanillaUnbakedModel.of(id);
    }

    @Override
    public UnbakedGeometry geometry() {
        return (slots, baker, state, name) -> {
            if (!(name instanceof ResolvedModel resolved)) return QuadCollection.EMPTY;
            BakedModel model = bake(resolved, baker, state);
            QuadCollection.Builder builder = new QuadCollection.Builder();
            RandomSource random = RandomSource.create(42);
            for (Direction side : BakedModel.SIDES) {
                for (BakedQuad quad : model.getQuads(null, side, random, ModelData.EMPTY, null)) {
                    if (side == null) builder.addUnculledFace(quad.toVanilla(null));
                    else builder.addCulledFace(side, quad.toVanilla(null));
                }
            }
            return builder.build();
        };
    }

    @Override
    public @Nullable Boolean ambientOcclusion() {
        return base.ambientOcclusion();
    }

    @Override
    public @Nullable GuiLight guiLight() {
        return base.guiLight();
    }

    @Override
    public @Nullable ItemTransforms transforms() {
        return base.transforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return base.textureSlots();
    }

    @Override
    public @Nullable Identifier parent() {
        return base.parent();
    }

    private record Context(ResolvedModel model, TextureSlots slots, ResolvedModel baseModel)
            implements IGeometryBakingContext {

        @Override
        public ResolvedModel getBaseModel() {
            return baseModel;
        }

        @Override
        public String getModelName() {
            return model.debugName();
        }

        @Override
        public boolean hasMaterial(String name) {
            return slots.getMaterial(name) != null;
        }

        @Override
        public @Nullable Material getMaterial(String name) {
            return slots.getMaterial(name);
        }

        @Override
        public boolean isGui3d() {
            return model.getTopGuiLight().lightLikeBlock();
        }

        @Override
        public boolean useBlockLight() {
            return model.getTopGuiLight().lightLikeBlock();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return model.getTopAmbientOcclusion();
        }

        @Override
        public ItemTransforms getTransforms() {
            return model.getTopTransforms();
        }
    }
}
