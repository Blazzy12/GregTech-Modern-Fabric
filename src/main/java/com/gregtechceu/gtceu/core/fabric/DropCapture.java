package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.world.entity.item.ItemEntity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Collects the item entities a living entity drops on death, for {@code LivingDropsEvent}. */
public final class DropCapture {

    private static final ThreadLocal<@Nullable List<ItemEntity>> CAPTURED = new ThreadLocal<>();

    private DropCapture() {}

    public static List<ItemEntity> start() {
        List<ItemEntity> drops = new ArrayList<>();
        CAPTURED.set(drops);
        return drops;
    }

    public static void stop() {
        CAPTURED.remove();
    }

    /** @return whether the item was captured instead of spawned */
    public static boolean capture(ItemEntity item) {
        List<ItemEntity> drops = CAPTURED.get();
        if (drops == null) return false;
        drops.add(item);
        return true;
    }
}
