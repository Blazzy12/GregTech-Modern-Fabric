package com.gregtechceu.gtceu.client.model.pipe;

import java.util.Set;

import java.util.HashSet;

import java.util.Collection;

import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;

import net.minecraft.client.resources.model.ModelBaker;

import net.minecraft.client.renderer.block.dispatch.ModelState;

import net.minecraft.client.resources.model.sprite.Material;

import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;

import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryBakingContext;
import com.gregtechceu.gtceu.compat.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.gregtechceu.gtceu.client.model.machine.MachineModelLoader.MISSING_MARKER;

public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    @Getter
    private final Map<@Nullable Direction, UnbakedModel> parts;
    @Getter
    private final Map<@NotNull Direction, UnbakedModel> restrictors;

    public UnbakedPipeModel(Map<@Nullable Direction, UnbakedModel> parts,
                            Map<@NotNull Direction, UnbakedModel> restrictors) {
        this.parts = parts;
        this.restrictors = restrictors;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<Direction, BakedModel> bakedParts = new IdentityHashMap<>();
        this.parts.forEach((direction, unbaked) -> {
            bakedParts.put(direction, unbaked.bake(baker, spriteGetter, modelState));
        });
        Map<Direction, BakedModel> bakedRestrictors = new IdentityHashMap<>();
        this.restrictors.forEach((direction, unbaked) -> {
            bakedRestrictors.put(direction, unbaked.bake(baker, spriteGetter, modelState));
        });
        return new BakedPipeModel(bakedParts, bakedRestrictors);
    }

    @Override
    public Collection<Identifier> getDependencies() {
        Set<Identifier> dependencies = new HashSet<>();
        for (UnbakedModel model : parts.values()) {
            if (model != null) dependencies.addAll(model.getDependencies());
        }
        for (UnbakedModel model : restrictors.values()) {
            if (model != null) dependencies.addAll(model.getDependencies());
        }
        return dependencies;
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {
        UnbakedModel missingModel = resolver.apply(MissingCuboidModel.LOCATION);

        Map<Direction, UnbakedModel> copy = new IdentityHashMap<>(this.parts);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.parts.put(side, missingModel);
            } else {
                variant.resolveParents(resolver);
                this.parts.put(side, variant);
            }
        });
        copy = new IdentityHashMap<>(this.restrictors);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.restrictors.put(side, missingModel);
            } else {
                variant.resolveParents(resolver);
                this.restrictors.put(side, variant);
            }
        });
    }
}
