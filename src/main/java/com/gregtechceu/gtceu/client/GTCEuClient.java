package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.compat.client.color.ColorHandlers;
import com.gregtechceu.gtceu.compat.client.event.RegisterColorHandlersEvent;
import com.gregtechceu.gtceu.compat.client.extensions.common.ClientExtensionsManager;
import com.gregtechceu.gtceu.compat.client.extensions.common.RegisterClientExtensionsEvent;
import com.gregtechceu.gtceu.compat.fml.ModBuses;
import com.gregtechceu.gtceu.compat.fml.event.lifecycle.FMLClientSetupEvent;
import com.gregtechceu.gtceu.core.fabric.FabricClientEventBridge;
import com.gregtechceu.gtceu.core.fabric.FabricEventBridge;

import brachy.modularui.client.ModularUIClient;

import net.fabricmc.api.ClientModInitializer;

/** GregTech's client entrypoint. */
public class GTCEuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ColorHandlers.bootstrap();
        FabricClientEventBridge.init();
        FabricEventBridge.registerSubscribers(ClientProxy.class, PipeModel.class);

        ModularUIClient.init();
        ClientProxy.init();

        RegisterClientExtensionsEvent extensions = new RegisterClientExtensionsEvent();
        ClientProxy.registerClientExtensions(extensions);
        ModBuses.MOD_BUS.post(extensions);
        ClientExtensionsManager.registerFluidModels();
        com.gregtechceu.gtceu.compat.transfer.FluidVariantRenderHandlers.init();

        // NeoForge asked for colour handlers once; Registrate's builders wait for these
        ModBuses.MOD_BUS.post(new RegisterColorHandlersEvent.Block());
        ModBuses.MOD_BUS.post(new RegisterColorHandlersEvent.Item());

        ModBuses.MOD_BUS.post(new FMLClientSetupEvent());
    }
}
