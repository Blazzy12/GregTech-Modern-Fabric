package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.common.CommonEventListener;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeBookCategories;
import com.gregtechceu.gtceu.common.data.worldgen.GTBiomeModifiers;
import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import com.gregtechceu.gtceu.common.module.CreativeFlightModule;
import com.gregtechceu.gtceu.compat.common.NeoForgeMod;
import com.gregtechceu.gtceu.compat.event.AddReloadListenerEvent;
import com.gregtechceu.gtceu.compat.fml.ModBuses;
import com.gregtechceu.gtceu.compat.fml.event.lifecycle.FMLCommonSetupEvent;
import com.gregtechceu.gtceu.compat.fml.event.lifecycle.FMLLoadCompleteEvent;
import com.gregtechceu.gtceu.compat.fml.loading.RegistrationLoader;
import com.gregtechceu.gtceu.compat.network.event.RegisterPayloadHandlersEvent;
import com.gregtechceu.gtceu.compat.registries.NeoForgeRegistries;
import com.gregtechceu.gtceu.compat.server.ServerLifecycleHooks;
import com.gregtechceu.gtceu.compat.transfer.TransferBridges;
import com.gregtechceu.gtceu.core.fabric.EarlyComponentBinder;
import com.gregtechceu.gtceu.core.fabric.FabricEventBridge;

import brachy.modularui.ModularUI;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.network.NetworkHandler;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * GregTech's common entrypoint. It replays what NeoForge did for the mod: construct it (content is registered
 * through deferred registers on the shared mod bus), fire the registration events, then the setup events.
 */
public class GTCEuFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NeoForgeRegistries.init();
        ServerLifecycleHooks.init();
        NeoForgeMod.init();

        FabricEventBridge.init();
        FabricEventBridge.registerSubscribers(CommonEventListener.class, ToolEventHandlers.class,
                TreeFellingHelper.class, CreativeFlightModule.class, GuiManager.class, NetworkHandler.class);

        EarlyComponentBinder.bind();
        GTCEu.init(ModBuses.MOD_BUS);
        ModularUI.init();
        GTRecipeBookCategories.init();

        RegistrationLoader.run();
        CommonProxy.registerLegacyAliases();
        TransferBridges.init();
        EarlyComponentBinder.bind();
        GTBlocks.registerBlockTransformers();
        GTBiomeModifiers.init();
        ModBuses.MOD_BUS.post(new RegisterPayloadHandlersEvent());

        List<PreparableReloadListener> reloadListeners = new ArrayList<>();
        NeoForgeEventsHolder.postReloadListeners(reloadListeners);
        for (int i = 0; i < reloadListeners.size(); i++) {
            ResourceLoader.get(PackType.SERVER_DATA)
                    .registerReloadListener(GTCEu.id("reload_listener_" + i), reloadListeners.get(i));
        }

        ModBuses.MOD_BUS.post(new FMLCommonSetupEvent());
        ModBuses.MOD_BUS.post(new FMLLoadCompleteEvent());
    }

    /** Server data reload listeners, registered through {@link AddReloadListenerEvent} like on NeoForge. */
    private static final class NeoForgeEventsHolder {

        static void postReloadListeners(List<PreparableReloadListener> listeners) {
            com.gregtechceu.gtceu.compat.common.NeoForge.EVENT_BUS.post(new AddReloadListenerEvent(listeners));
        }
    }
}
