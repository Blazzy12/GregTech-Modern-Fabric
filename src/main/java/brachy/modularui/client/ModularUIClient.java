package brachy.modularui.client;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIMenuTypes;
import brachy.modularui.animation.AnimatorManager;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.DrawableTooltipComponent;
import brachy.modularui.drawable.HoverableIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.InteractableIcon;
import brachy.modularui.drawable.TooltipComponentIcon;
import brachy.modularui.drawable.text.KeyIcon;
import brachy.modularui.drawable.schema.SchemaPipRenderer;
import brachy.modularui.drawable.text.TextIcon;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.theme.ThemeManager;
import brachy.modularui.utils.CursorHandler;
import brachy.modularui.utils.RegistryAccessContainer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import lombok.Getter;

/** ModularUI's client setup; {@link #init()} is called from the client entrypoint. */
public final class ModularUIClient {

    @Getter
    private static final DeltaTracker.Timer timer60Fps = new DeltaTracker.Timer(60f, 0, FloatUnaryOperator.identity());

    private ModularUIClient() {}

    @SuppressWarnings("deprecation")
    public static void init() {
        CursorHandler.init();
        AnimatorManager.init();
        ClientScreenHandler.registerEvents();

        PictureInPictureRendererRegistry.register(context -> new SchemaPipRenderer());

        MenuScreens.<ModularContainerMenu, ContainerScreenWrapper>register(ModularUIMenuTypes.MODULAR_CONTAINER.get(),
                ContainerScreenWrapper::new);

        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof TooltipComponentIcon icon) return icon.clientComponent();
            if (data instanceof Icon || data instanceof DelegateIcon || data instanceof HoverableIcon ||
                    data instanceof InteractableIcon || data instanceof KeyIcon || data instanceof TextIcon) {
                return new DrawableTooltipComponent((IIcon) data);
            }
            return null;
        });

        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(Identifier.fromNamespaceAndPath(ModularUI.MOD_ID, "themes"),
                        ThemeManager.INSTANCE);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onUnloadWorld());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // an integrated server already updated these with its own registries
            if (!client.hasSingleplayerServer()) RegistryAccessContainer.update(handler.registryAccess());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) ModularUI.onTick(client.player);
        });
    }

    private static void onUnloadWorld() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ModularNetwork.CLIENT.onPlayerLeave(player);

            if (Minecraft.getInstance().hasSingleplayerServer()) {
                // we need to handle single player here, since PlayerLoggedOutEvent is not triggered for some reason
                ModularNetwork.SERVER.onPlayerLeave(player);
            }
        }
    }
}
