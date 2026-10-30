package com.gregtechceu.gtceu.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;

import com.gregtechceu.gtceu.client.renderer.item.FacadeSpecialRenderer;

import com.gregtechceu.gtceu.client.renderer.item.MachineItemSpecialRenderer;

import com.gregtechceu.gtceu.client.renderer.item.LampSpecialRenderer;

import net.minecraft.client.renderer.special.SpecialModelRenderers;

import com.gregtechceu.gtceu.client.model.composite.CompositeModel;

import com.gregtechceu.gtceu.compat.client.model.geometry.GeometryLoaderModel;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.client.model.item.CustomItemRendererWrapperModel;
import com.gregtechceu.gtceu.client.model.item.FacadeUnbakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModelLoader;
import com.gregtechceu.gtceu.client.model.runtimegen.*;
import com.gregtechceu.gtceu.client.particle.GTParticleManager;
import com.gregtechceu.gtceu.client.particle.HazardParticle;
import com.gregtechceu.gtceu.client.particle.MufflerParticle;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTComponentItemDecorator;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTLampItemOverlayRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTTankItemFluidPreview;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTToolBarRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.impl.*;
import com.gregtechceu.gtceu.client.renderer.machine.impl.BoilerMultiPartRender;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;
import com.gregtechceu.gtceu.common.CommonEventListener;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.gregtechceu.gtceu.common.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import com.gregtechceu.gtceu.compat.bus.EventPriority;
import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.bus.SubscribeEvent;
import com.gregtechceu.gtceu.compat.fml.event.lifecycle.FMLClientSetupEvent;
import com.gregtechceu.gtceu.compat.client.event.*;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;
import com.gregtechceu.gtceu.compat.client.extensions.common.RegisterClientExtensionsEvent;
import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class ClientProxy {

    /** GregTech's client setup, called from the client entrypoint. */
    public static void init() {
        ClientCacheManager.registerClientCache(GTClientCache.instance, "gtceu");
        Layers.registerLayer(OreRenderLayer::new, "ore_veins");
        Layers.registerLayer(FluidRenderLayer::new, "bedrock_fluids");
        CommonEventListener.registerCapes(new RegisterGTCapesEvent());

        registerModelLoaders();
        registerSpecialItemRenderers();
        initializeDynamicRenders();
        ModelEventHelper.initInternalAssetReloadListeners();

        GTParticleManager.init();
        BloomRenderer.init();
        GTGuiTextures.init();
        GTGuiTheme.registerThemes();

        registerEntityRenderers();
        registerKeyBindings();
        registerItemDecorations();
        HudElementRegistry.addLast(GTCEu.id("hud"), new HudGuiOverlay());
        registerParticleProviders();
        ClientEventListener.init();
    }

    public static final net.minecraft.client.model.geom.ModelLayerLocation RUBBER_BOAT_LAYER = boatLayer("boat/rubber");
    public static final net.minecraft.client.model.geom.ModelLayerLocation RUBBER_CHEST_BOAT_LAYER = boatLayer(
            "chest_boat/rubber");
    public static final net.minecraft.client.model.geom.ModelLayerLocation TREATED_WOOD_BOAT_LAYER = boatLayer(
            "boat/treated_wood");
    public static final net.minecraft.client.model.geom.ModelLayerLocation TREATED_WOOD_CHEST_BOAT_LAYER = boatLayer(
            "chest_boat/treated_wood");

    private static net.minecraft.client.model.geom.ModelLayerLocation boatLayer(String path) {
        return new net.minecraft.client.model.geom.ModelLayerLocation(GTCEu.id(path), "main");
    }

    /** Called from the client entrypoint. Boat textures follow the layer id: {@code textures/entity/boat/*.png}. */
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        EntityRendererRegistry.register(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);

        ModelLayerRegistry.registerModelLayer(RUBBER_BOAT_LAYER, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(TREATED_WOOD_BOAT_LAYER, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(RUBBER_CHEST_BOAT_LAYER, BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(TREATED_WOOD_CHEST_BOAT_LAYER, BoatModel::createChestBoatModel);
        EntityRendererRegistry.register(GTEntityTypes.RUBBER_BOAT.get(), c -> new BoatRenderer(c, RUBBER_BOAT_LAYER));
        EntityRendererRegistry.register(GTEntityTypes.TREATED_WOOD_BOAT.get(),
                c -> new BoatRenderer(c, TREATED_WOOD_BOAT_LAYER));
        EntityRendererRegistry.register(GTEntityTypes.RUBBER_CHEST_BOAT.get(),
                c -> new BoatRenderer(c, RUBBER_CHEST_BOAT_LAYER));
        EntityRendererRegistry.register(GTEntityTypes.TREATED_WOOD_CHEST_BOAT.get(),
                c -> new BoatRenderer(c, TREATED_WOOD_CHEST_BOAT_LAYER));
    }

    /** Draws GregTech's item overlays (charge bars, tool durability, lamp state, tank contents). */
    private static void registerItemDecorations() {
        ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) -> {
            Item item = stack.getItem();
            if (item instanceof IComponentItem) GTComponentItemDecorator.INSTANCE.render(graphics, font, stack, x, y);
            if (item instanceof IGTTool) GTToolBarRenderer.INSTANCE.render(graphics, font, stack, x, y);
            if (item instanceof LampBlockItem) GTLampItemOverlayRenderer.INSTANCE.render(graphics, font, stack, x, y);
            if (item instanceof DrumMachineItem) GTTankItemFluidPreview.DRUM.render(graphics, font, stack, x, y);
            if (item instanceof QuantumTankMachineItem) {
                GTTankItemFluidPreview.QUANTUM_TANK.render(graphics, font, stack, x, y);
            }
        });
    }

    /** Called from the client entrypoint. */
    public static void registerKeyBindings() {
        SyncedKeyMapping.registerClient();
    }

    private static void registerParticleProviders() {
        ParticleProviderRegistry.getInstance().register(GTParticleTypes.HAZARD_PARTICLE.get(),
                HazardParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GTParticleTypes.MUFFLER_PARTICLE.get(),
                MufflerParticle.Provider::new);
    }

    private static void initializeDynamicRenders() {
        DynamicRenderManager.register(GTCEu.id("quantum_tank_fluid"), QuantumTankFluidRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("quantum_chest_item"), QuantumChestItemRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fusion_ring"), FusionRingRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("boiler_multi_parts"), BoilerMultiPartRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("assembly_line"), AssemblyLineRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fluid_area"), FluidAreaRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("growing_plant"), GrowingPlantRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("central_monitor"), CentralMonitorRender.TYPE);
    }

    /** Registers GregTech's special item renderers ({@code "minecraft:special"} item models). */
    public static void registerSpecialItemRenderers() {
        SpecialModelRenderers.ID_MAPPER.put(GTCEu.id("lamp"), LampSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(GTCEu.id("machine_dynamic"), MachineItemSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(GTCEu.id("facade"), FacadeSpecialRenderer.Unbaked.MAP_CODEC);
    }

    /** Registers GregTech's custom model types ({@code "fabric:type"} in model JSON). */
    public static void registerModelLoaders() {
        GeometryLoaderModel.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
        GeometryLoaderModel.register(PipeModelLoader.ID, PipeModelLoader.INSTANCE);
        GeometryLoaderModel.register(GTCEu.id("facade"), FacadeUnbakedModel.Loader.INSTANCE);
        GeometryLoaderModel.register(CustomItemRendererWrapperModel.ID, CustomItemRendererWrapperModel.Loader.INSTANCE);
        GeometryLoaderModel.register(CompositeModel.ID, CompositeModel.Loader.INSTANCE);
    }

    /** The client extensions of GregTech's fluid types; called from {@link #init}. */
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {

            private static final Identifier TEXTURE = GTCEu.id("block/fluids/fluid.potion");

            @Override
            public @NotNull Identifier getStillTexture() {
                return TEXTURE;
            }

            @Override
            public @NotNull Identifier getFlowingTexture() {
                return TEXTURE;
            }

            @Override
            public int getTintColor(@NotNull FluidStack stack) {
                return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .getColor() | 0xff000000;
            }
        }, GTFluids.POTION.getType());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        PipeModel.DYNAMIC_MODELS.clear();
    }

    @SubscribeEvent
    public static void registerDynamicAssets(RegisterDynamicResourcesEvent event) {
        // regenerate all pipe models in case their textures changed
        // cables may do this, others too if something's removed
        for (var block : GTMaterialBlocks.CABLE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.FLUID_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.ITEM_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }

        MaterialBlockModelGenerator.reinitModels();
        TagPrefixItemModelGenerator.reinitModels();
        OreBlockModelGenerator.reinitModels();
        ToolItemModelGenerator.reinitModels();
        ArmorItemModelGenerator.reinitModels();
        SurfaceRockModelGenerator.reinitModels();
        GTModels.registerMaterialFluidModels();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        // do this last so addons can easily add new variants to the registered model set
        PipeModel.initDynamicModels();

        RuntimeBlockstateProvider.INSTANCE.run();
        PipeModelBuilder.clearRestrictorModelCache();
    }
}
