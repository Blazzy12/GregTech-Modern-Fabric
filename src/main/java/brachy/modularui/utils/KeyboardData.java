package brachy.modularui.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.network.FriendlyByteBuf;
import com.gregtechceu.gtceu.compat.dist.Dist;


public record KeyboardData(Dist side, int keyCode, int scanCode, int modifiers) {

    public boolean hasControlDown() {
        return (this.modifiers & InputConstants.MOD_CONTROL) != 0;
    }

    public boolean hasShiftDown() {
        return (this.modifiers & InputConstants.MOD_SHIFT) != 0;
    }

    public boolean hasAltDown() {
        return (this.modifiers & InputConstants.MOD_ALT) != 0;
    }

    public boolean isClient() {
        return this.side.isClient();
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.keyCode);
        buffer.writeVarInt(this.scanCode);
        buffer.writeVarInt(this.modifiers);
    }

    public static KeyboardData readPacket(FriendlyByteBuf buffer) {
        int keyCode = buffer.readVarInt();
        int scanCode = buffer.readVarInt();
        int modifiers = buffer.readVarInt();
        return new KeyboardData(Dist.DEDICATED_SERVER, keyCode, scanCode, modifiers);
    }

    @Environment(EnvType.CLIENT)
    public static KeyboardData create(int keyCode, int scanCode, int modifiers) {
        return new KeyboardData(Dist.CLIENT, keyCode, scanCode, modifiers);
    }
}
