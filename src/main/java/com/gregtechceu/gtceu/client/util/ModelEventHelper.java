package com.gregtechceu.gtceu.client.util;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import net.minecraft.core.Direction;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.data.AtlasIds;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import com.gregtechceu.gtceu.client.model.ctm.GTTextureMetadata;

import com.gregtechceu.gtceu.compat.client.model.VanillaBakedModel;

import com.gregtechceu.gtceu.compat.client.model.GTBlockStateModel;

import com.gregtechceu.gtceu.compat.client.event.TextureAtlasStitchedEvent;



import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.compat.client.extensions.common.ClientExtensionsManager;
import com.gregtechceu.gtceu.client.model.ctm.CTMBakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.dist.Dist;
import com.gregtechceu.gtceu.compat.fml.common.EventBusSubscriber;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
@UtilityClass
@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class ModelEventHelper {

    @ApiStatus.Internal
    public record EventListenerHolder<T>(T listener, boolean removeOnReload) {}

    @ApiStatus.Internal
    public static final List<EventListenerHolder<?>> EVENT_LISTENERS = new ArrayList<>();
    private static final Map<Identifier, Optional<TextureAtlasSprite>> CTM_SPRITE_CACHE = new ConcurrentHashMap<>();

    public static void registerAtlasStitchedEventListener(boolean removeOnReload,
                                                          AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add(new EventListenerHolder<>(listener, removeOnReload));
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload, final Identifier atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        registerAtlasStitchedEventListener(removeOnReload, event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    private static final AtomicInteger reloadCounter = new AtomicInteger(0);

    /** Called when the model manager starts reloading. */
    @ApiStatus.Internal
    public static void onReload() {
        if (reloadCounter.addAndGet(1) > 1) {
            EVENT_LISTENERS.removeIf(EventListenerHolder::removeOnReload);
        }
        CTM_SPRITE_CACHE.clear();
        TextureMetadataHelper.invalidateCaches();
    }

    /** Called when the model manager applied new models; the atlases are ready by then. */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static void onModelsApplied() {
        var atlases = Minecraft.getInstance().getAtlasManager();
        for (Identifier id : List.of(AtlasIds.BLOCKS, AtlasIds.ITEMS)) {
            TextureAtlas atlas = atlases.getAtlasOrThrow(id);
            var event = new TextureAtlasStitchedEvent(atlas);
            for (var listener : EVENT_LISTENERS) {
                if (listener.listener instanceof AssetEventListener<?> assetEventListener &&
                        assetEventListener.eventClass() == TextureAtlasStitchedEvent.class) {
                    ((AssetEventListener<TextureAtlasStitchedEvent>) assetEventListener).accept(event);
                }
            }
        }
    }

    /**
     * @return the connected-texture sprite of a sprite, if its texture has one in its {@code gtceu} metadata
     */
    public static @Nullable TextureAtlasSprite getConnectionSprite(TextureAtlasSprite sprite) {
        return CTM_SPRITE_CACHE.computeIfAbsent(sprite.contents().name(), location -> TextureMetadataHelper
                .getMetadataFromRelativeLocation(location)
                .flatMap(GTTextureMetadata::connectionTexture)
                .map(texture -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                        .getSprite(texture)))
                .orElse(null);
    }

    // INTERNAL ASSET RELOAD LISTENER REGISTRATION

    /** Wraps block models with connected textures in a {@link CTMBakedModel}. */
    @ApiStatus.Internal
    public static void initInternalAssetReloadListeners() {
        registerAtlasStitchedEventListener(false, event -> {
            TextureAtlas atlas = event.getAtlas();
            if (atlas != Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)) return;
            MachineModel.initSprites(atlas);
            ICoverableRenderer.initSprites(atlas);
        });

        // fluid textures depend on loaded resources, so fluid models are (re)registered at the start of each model
        // reload; Fabric reads them when it bakes the fluid models later in the same reload
        ModelLoadingPlugin.register(context -> ClientExtensionsManager.registerFluidModels());
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register((model, ctx) -> {
            if (model instanceof GTBlockStateModel gtModel) {
                return gtModel.model() instanceof CTMBakedModel<?> ? model : wrapIfConnected(gtModel.model(), model,
                        ctx.state());
            }
            return wrapIfConnected(null, model, ctx.state());
        }));
    }

    private static BlockStateModel wrapIfConnected(@Nullable BakedModel gtModel, BlockStateModel model,
                                                   BlockState state) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42), parts);
        for (BlockStateModelPart part : parts) {
            for (Direction side : BakedModel.SIDES) {
                for (var quad : part.getQuads(side)) {
                    if (TextureMetadataHelper.getMetadata(quad.materialInfo().sprite())
                            .flatMap(GTTextureMetadata::connectionTexture).isPresent()) {
                        BakedModel base = gtModel != null ? gtModel : new VanillaBakedModel(model);
                        return new GTBlockStateModel(new CTMBakedModel<>(base), state);
                    }
                }
            }
        }
        return model;
    }
}
