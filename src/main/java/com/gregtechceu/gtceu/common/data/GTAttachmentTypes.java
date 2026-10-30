package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.core.fabric.Registries;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

public class GTAttachmentTypes {

    /**
     * Persists a player's {@link MedicalConditionTracker}. The tracker needs its player, which Fabric attachment
     * initializers do not get, so the saved tag is kept until the tracker is first requested.
     */
    public static final AttachmentType<TrackerData> MEDICAL_CONDITION_TRACKER = AttachmentRegistry
            .<TrackerData>builder()
            .persistent(CompoundTag.CODEC.xmap(TrackerData::new, TrackerData::save))
            .initializer(() -> new TrackerData(null))
            .buildAndRegister(GTCEu.id("hazard_tracker"));

    public static MedicalConditionTracker getMedicalConditionTracker(Player player) {
        TrackerData data = player.getAttachedOrCreate(MEDICAL_CONDITION_TRACKER);
        if (data.tracker == null) {
            data.tracker = new MedicalConditionTracker(player);
            if (data.pending != null) {
                data.tracker.deserializeNBT(player.registryAccess(), data.pending);
                data.pending = null;
            }
        }
        return data.tracker;
    }

    public static void init() {}

    public static final class TrackerData {

        private @Nullable CompoundTag pending;
        private @Nullable MedicalConditionTracker tracker;

        private TrackerData(@Nullable CompoundTag pending) {
            this.pending = pending;
        }

        private CompoundTag save() {
            if (tracker != null) return tracker.serializeNBT(Registries.access());
            return pending != null ? pending : new CompoundTag();
        }
    }
}
