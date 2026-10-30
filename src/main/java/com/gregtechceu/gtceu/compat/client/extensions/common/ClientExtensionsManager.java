package com.gregtechceu.gtceu.compat.client.extensions.common;

import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ClientExtensionsManager {

    static final Map<FluidType, IClientFluidTypeExtensions> FLUID_TYPE_EXTENSIONS = new IdentityHashMap<>();
    static final Map<Item, IClientItemExtensions> ITEM_EXTENSIONS = new IdentityHashMap<>();
    static final Map<Block, IClientBlockExtensions> BLOCK_EXTENSIONS = new IdentityHashMap<>();

    private ClientExtensionsManager() {}

    /**
     * Gives every registered fluid whose type has client extensions a fluid model built from the extension's
     * textures and tint. Called once on the client after all fluids are registered.
     */
    public static void registerFluidModels() {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            FluidType type = FluidType.of(fluid);
            IClientFluidTypeExtensions ext = FLUID_TYPE_EXTENSIONS.get(type);
            if (ext == null || ext.getStillTexture() == null) continue;
            Identifier flowing = ext.getFlowingTexture() != null ? ext.getFlowingTexture() : ext.getStillTexture();
            Identifier overlay = ext.getOverlayTexture();
            int tint = ext.getTintColor();
            FluidRenderingRegistry.register(fluid, new FluidModel.Unbaked(new Material(ext.getStillTexture()),
                    new Material(flowing), overlay == null ? null : new Material(overlay),
                    tint == -1 ? null : BlockTintSources.constant(tint)));
        }
    }
}
