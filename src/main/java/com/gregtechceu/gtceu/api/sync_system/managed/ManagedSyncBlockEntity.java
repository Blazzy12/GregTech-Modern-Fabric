package com.gregtechceu.gtceu.api.sync_system.managed;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.gregtechceu.gtceu.core.fabric.ValueIO;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A BlockEntity that manages sync and save data via the {@code ISyncManaged} syncdata system.
 * 
 * @see ISyncManaged
 */
public abstract class ManagedSyncBlockEntity extends BlockEntity implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    @Setter
    private static final String SYNC_DATA_KEY = "gtceu:sync_data";

    private boolean isDirty;

    public ManagedSyncBlockEntity(BlockEntityCreationInfo info) {
        super(info.type(), info.pos(), info.state());
    }

    public ManagedSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Saves BE data to world save.
     */
    @Override
    protected final void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueIO.write(output, getSyncDataHolder().serializeNBT(getRegistryAccess()));
    }

    /**
     * Loads BE data from world save.<br>
     * Override this to add logic for modifying saved data before it is loaded (e.g. for cross-version
     * compatibility).<br>
     * When overriding, {@code super.load(tag)} must be called <b>AFTER</b> any custom logic.
     *
     * @param tag        The tag to load
     * @param registries Registry lookup
     */
    @Override
    protected final void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        var syncData = input.getOptionalByteArray(SYNC_DATA_KEY);
        if (syncData.isPresent()) {
            // update tag or update packet sent to the client
            var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(syncData.get()),
                    (RegistryAccess) input.lookup());
            getSyncDataHolder().readClientPacket(input.lookup(), buffer);
        } else {
            loadAdditional(ValueIO.read(input), input.lookup());
        }
    }

    /**
     * Loads BE data from world save.
     */
    @MustBeInvokedByOverriders
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        getSyncDataHolder().deserializeNBT(registries, tag);
    }

    private HolderLookup.Provider getRegistryAccess() {
        if (getLevel() != null) return getLevel().registryAccess();
        return com.gregtechceu.gtceu.core.fabric.Registries.access();
    }

    /**
     * Called to gather BE data to be sent when a client loads this BE.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        return writeClientPacket(lookup, true);
    }

    /**
     * Called to get an update packet which is sent to clients to notify them when a loaded BE's data changes.
     */
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (b, r) -> writeClientPacket(r, false));
    }

    private CompoundTag writeClientPacket(HolderLookup.Provider lookup, boolean fullSync) {
        var stream = new RegistryFriendlyByteBuf(Unpooled.buffer(), (RegistryAccess) lookup);
        if (fullSync) getSyncDataHolder().resyncAllFields();
        getSyncDataHolder().writeClientPacket(lookup, stream);

        stream.capacity(stream.readableBytes());
        CompoundTag data = new CompoundTag();
        data.putByteArray(SYNC_DATA_KEY, stream.array());
        return data;
    }

    @Override
    public @Nullable ISyncManaged getParentSyncObject() {
        return null;
    }

    @Override
    public final void markAsChanged() {
        isDirty = true;
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    /**
     * Called each tick on the server side.
     */
    @MustBeInvokedByOverriders
    public void serverTick() {
        setChanged();
        if (isDirty) {
            Objects.requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
            isDirty = false;
        }
    }

    /**
     * Called each tick on the client side.
     */
    public void clientTick() {}
}
