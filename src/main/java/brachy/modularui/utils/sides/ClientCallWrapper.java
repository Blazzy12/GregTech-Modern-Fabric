package brachy.modularui.utils.sides;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

/**
 * Internal helper class acting as a safeguard for accessing client-only methods
 */
/* package-private */ final class ClientCallWrapper {

    private ClientCallWrapper() {}

    static RegistryAccess.Frozen getClientRegistries() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }
}
