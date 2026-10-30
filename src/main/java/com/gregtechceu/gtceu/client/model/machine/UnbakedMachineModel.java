package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;

import java.util.Set;

import java.util.HashSet;

import java.util.Collection;

import net.minecraft.client.resources.model.ModelBaker;

import net.minecraft.client.renderer.block.dispatch.ModelState;

import net.minecraft.client.resources.model.sprite.Material;

import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryBakingContext;
import com.gregtechceu.gtceu.compat.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    @Getter
    private final MachineDefinition definition;
    @Getter
    private final Map<MachineRenderState, UnbakedModel> models;
    @Nullable
    @Getter
    private final MultiPartUnbakedModel multiPart;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;
    private final Set<String> replaceableTextures;
    private final Map<String, Identifier> textureOverrides;

    public UnbakedMachineModel(MachineDefinition definition,
                               Map<MachineRenderState, UnbakedModel> models,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicRender<?, ?>> dynamicRenders,

                               Set<String> replaceableTextures,
                               Map<String, Identifier> textureOverrides) {
        this.definition = definition;
        this.models = models;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;
        this.replaceableTextures = replaceableTextures;
        this.textureOverrides = textureOverrides;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<String, TextureAtlasSprite> textureOverrides = new HashMap<>();
        for (var entry : this.textureOverrides.entrySet()) {
            Material material = new Material(entry.getValue());
            textureOverrides.put(entry.getKey(), spriteGetter.apply(material));
        }

        Map<MachineRenderState, BakedModel> baseModels = new IdentityHashMap<>();
        models.forEach((machineState, unbaked) -> {
            baseModels.put(machineState, unbaked.bake(baker, spriteGetter, modelState));
        });
        MultiPartBakedModel multiPart = this.multiPart == null ? null :
                this.multiPart.bake(baker, spriteGetter, modelState);

        MachineModel model = new MachineModel(this.getDefinition(), baseModels, multiPart, this.dynamicRenders,
                context.getTransforms(), context.getRootTransform(), modelState,
                context.isGui3d(), context.useBlockLight(), context.useAmbientOcclusion());

        if (context.hasMaterial("particle")) {
            model.setParticleIcon(spriteGetter.apply(context.getMaterial("particle")));
        }
        model.setReplaceableTextures(this.replaceableTextures);
        model.setTextureOverrides(textureOverrides);
        return model;
    }

    @Override
    public Collection<Identifier> getDependencies() {
        Set<Identifier> dependencies = new HashSet<>();
        for (UnbakedModel model : models.values()) {
            dependencies.addAll(model.getDependencies());
        }
        if (multiPart != null) {
            dependencies.addAll(multiPart.getDependencies());
        }
        return dependencies;
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {
        MachineModelLoader.resolveStateModels(this, resolver);
    }
}
