package brachy.modularui.utils.sides;

import brachy.modularui.ModularUI;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import com.gregtechceu.gtceu.compat.network.connection.ConnectionType;
import com.gregtechceu.gtceu.compat.server.ServerLifecycleHooks;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

public final class SidedAccessHelper {

    private SidedAccessHelper() {}

    public static RegistryAccess getRegistries() {
        if (ModularUI.isClientThread()) {
            return ClientCallWrapper.getClientRegistries();
        } else {
            return getServer().registryAccess();
        }
    }

    public static RegistryFriendlyByteBuf makeRegistryByteBuf(ByteBuf buffer) {
        return new RegistryFriendlyByteBuf(buffer, getRegistries());
    }

    private static @Nullable MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
