package com.gregtechceu.gtceu.compat.common.extensions;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Menu types whose opening packet carries extra data, as Fabric extended menu types. */
public final class IMenuTypeExtension {

    private IMenuTypeExtension() {}

    @FunctionalInterface
    public interface IContainerFactory<T extends AbstractContainerMenu> {

        T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);
    }

    /** The opening data is the raw bytes written by the server when opening the menu. */
    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new ExtendedMenuType<>((windowId, inv, data) -> {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data),
                    inv.player.registryAccess());
            try {
                return factory.create(windowId, inv, buf);
            } finally {
                buf.release();
            }
        }, ByteBufCodecs.BYTE_ARRAY.cast());
    }

    /** NeoForge's {@code ServerPlayer#openMenu(MenuProvider, Consumer)}: opens a menu, sending extra data. */
    public static java.util.OptionalInt openMenu(net.minecraft.server.level.ServerPlayer player,
                                                 net.minecraft.world.MenuProvider provider,
                                                 java.util.function.Consumer<RegistryFriendlyByteBuf> extraData) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),
                player.registryAccess());
        byte[] data;
        try {
            extraData.accept(buf);
            data = new byte[buf.readableBytes()];
            buf.readBytes(data);
        } finally {
            buf.release();
        }
        return player.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<byte[]>() {

            @Override
            public byte[] getScreenOpeningData(net.minecraft.server.level.ServerPlayer serverPlayer) {
                return data;
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory,
                                                    net.minecraft.world.entity.player.Player player) {
                return provider.createMenu(id, inventory, player);
            }
        });
    }
}
