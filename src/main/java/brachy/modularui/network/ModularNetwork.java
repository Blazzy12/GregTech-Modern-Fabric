package brachy.modularui.network;

import com.gregtechceu.gtceu.compat.dist.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.IPacketWriter;
import brachy.modularui.network.packets.SyncHandlerPacket;
import brachy.modularui.utils.NetworkUtils;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.SyncHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.gregtechceu.gtceu.compat.network.PacketDistributor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public abstract class ModularNetwork {

    // You have to make sure you are choosing the logical side you are currently on otherwise you can mess things up badly,
    // since there is no validation.
    public static final Client CLIENT = new Client();
    public static final ServerManager SERVER = new ServerManager();

    public static ModularNetworkSide get(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(Dist side) {
        return side.isClient() ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(Player player) {
        return get(NetworkUtils.isClient(player));
    }

    public static final class Client extends ModularNetworkSide {

        @Override
        public boolean isClient() {
            return true;
        }

        public void activate(int nid, ModularSyncManager msm) {
            activateInternal(nid, msm);
        }

        @Environment(EnvType.CLIENT)
        @Override
        void sendPacket(CustomPacketPayload packet, Player player) {
            PacketDistributor.sendToServer(packet);
        }

        @Environment(EnvType.CLIENT)
        @Override
        void closeContainer(Player player) {
            // mimics LocalPlayer.clientSideCloseContainer() but without closing the screen
            player.containerMenu = player.inventoryMenu;
        }

        @Environment(EnvType.CLIENT)
        public void closeContainer(int networkId, boolean dispose, Player player) {
            closeContainer(networkId, dispose, player, true);
        }

        @Environment(EnvType.CLIENT)
        public void closeAll() {
            closeAll(Minecraft.getInstance().player);
        }

        @Environment(EnvType.CLIENT)
        public void reopenSyncerOf(Screen guiScreen) {
            if (guiScreen instanceof IMuiScreen ms && !ms.screen().isClientOnly()) {
                ModularSyncManager msm = ms.screen().getSyncManager();
                reopen(Minecraft.getInstance().player, msm, true);
            }
        }
    }

    public static final class ServerManager extends Server {

        private final Map<UUID, Server> playerHandlers = new Object2ObjectOpenHashMap<>();

        public Server get(Player player) {
            return playerHandlers.computeIfAbsent(player.getUUID(), k -> new Server());
        }

        public int activate(Player player, ModularSyncManager msm) {
            return get(player).activate(msm);
        }

        @Override
        public void onPlayerLeave(Player player) {
            get(player).onPlayerLeave(player);
            this.playerHandlers.remove(player.getUUID());
        }

        @Override
        public void closeAll(Player player) {
            get(player).closeAll(player);
        }

        @Override
        public void closeAll(Player player, boolean sync) {
            get(player).closeAll(player, sync);
        }

        @Override
        public void receivePacket(Player player, SyncHandlerPacket packet) {
            get(player).receivePacket(player, packet);
        }

        @Override
        public void sendSyncHandlerPacket(String panel, SyncHandler<?> syncHandler, IPacketWriter<? super RegistryFriendlyByteBuf> writer, Player player) {
            get(player).sendSyncHandlerPacket(panel, syncHandler, writer, player);
        }

        @Override
        public void sendActionPacket(ModularSyncManager msm, String panel, String key, IPacketWriter<? super RegistryFriendlyByteBuf> writer, Player player) {
            get(player).sendActionPacket(msm, panel, key, writer, player);
        }

        @Override
        public void closeContainer(int networkId, boolean dispose, Player player, boolean sync) {
            get(player).closeContainer(networkId, dispose, player, sync);
        }

        @Override
        public void reopen(Player player, int networkId, boolean sync) {
            get(player).reopen(player, networkId, sync);
        }

        @Override
        public void reopen(Player player, ModularSyncManager msm, boolean sync) {
            get(player).reopen(player, msm, sync);
        }
    }

    @ApiStatus.NonExtendable
    public static class Server extends ModularNetworkSide {

        private int nextId = -1;

        protected int activate(ModularSyncManager msm) {
            if (++nextId > 100_000) nextId = 0;
            activateInternal(nextId, msm);
            return nextId;
        }

        @Override
        public boolean isClient() {
            return false;
        }

        @Override
        protected void sendPacket(CustomPacketPayload packet, Player player) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
        }

        @Override
        void closeContainer(Player player) {
            ((ServerPlayer) player).closeContainer();
        }

        public void closeContainer(int networkId, boolean dispose, ServerPlayer player) {
            closeContainer(networkId, dispose, player, true);
        }
    }
}
