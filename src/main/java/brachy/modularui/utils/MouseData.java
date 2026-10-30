package brachy.modularui.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import brachy.modularui.api.widget.Interactable;

import net.minecraft.network.VarInt;
import com.mojang.blaze3d.platform.InputConstants;
import com.gregtechceu.gtceu.compat.dist.Dist;

import io.netty.buffer.ByteBuf;

public record MouseData(Dist side, int mouseButton, boolean shift, boolean ctrl, boolean alt) {

    public boolean isClient() {
        return this.side.isClient();
    }

    public boolean isLeftMouseButton() {
        return this.mouseButton == InputConstants.MOUSE_BUTTON_LEFT;
    }

    public boolean isRightMouseButton() {
        return this.mouseButton == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    public boolean isMiddleMouseButton() {
        return this.mouseButton == InputConstants.MOUSE_BUTTON_MIDDLE;
    }

    public boolean isScrollUp() {
        return this.mouseButton > 0;
    }

    public boolean isScrollDown() {
        return this.mouseButton < 0;
    }

    public void writeToPacket(ByteBuf buffer) {
        VarInt.write(buffer, this.mouseButton);
        byte data = 0;
        if (this.shift) data |= 1;
        if (this.ctrl) data |= 2;
        if (this.alt) data |= 4;
        buffer.writeByte(data);
    }

    public static MouseData readPacket(ByteBuf buffer) {
        int button = VarInt.read(buffer);
        byte data = buffer.readByte();
        return new MouseData(Dist.DEDICATED_SERVER, button, (data & 1) != 0, (data & 2) != 0, (data & 4) != 0);
    }

    @Environment(EnvType.CLIENT)
    public static MouseData create(int mouse) {
        return new MouseData(Dist.CLIENT, mouse,
                Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
    }
}
