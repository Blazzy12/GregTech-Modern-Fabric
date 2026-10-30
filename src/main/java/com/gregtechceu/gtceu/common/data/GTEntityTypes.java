package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.entity.*;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import com.tterrag.registrate.util.entry.EntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTEntityTypes {

    public static final EntityEntry<DynamiteEntity> DYNAMITE = REGISTRATE
            .<DynamiteEntity>entity("dynamite", DynamiteEntity::new, MobCategory.MISC)
            .lang("Dynamite")
            .properties(builder -> builder.sized(0.25F, 0.25F).fireImmune().clientTrackingRange(4).updateInterval(10))
            .tag(EntityTypeTags.IMPACT_PROJECTILES)
            .register();

    public static final EntityEntry<PowderbarrelEntity> POWDERBARREL = REGISTRATE
            .<PowderbarrelEntity>entity("powderbarrel", PowderbarrelEntity::new, MobCategory.MISC)
            .lang("Powderbarrel")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    public static final EntityEntry<IndustrialTNTEntity> INDUSTRIAL_TNT = REGISTRATE
            .<IndustrialTNTEntity>entity("industrial_tnt", IndustrialTNTEntity::new, MobCategory.MISC)
            .lang("Industrial TNT")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    // 26.x has one entity type per boat kind instead of Boat.Type
    public static final EntityEntry<Boat> RUBBER_BOAT = boat("rubber_boat", "Rubber Boat",
            () -> GTItems.RUBBER_BOAT.get());
    public static final EntityEntry<ChestBoat> RUBBER_CHEST_BOAT = chestBoat("rubber_chest_boat",
            "Rubber Boat with Chest", () -> GTItems.RUBBER_CHEST_BOAT.get());
    public static final EntityEntry<Boat> TREATED_WOOD_BOAT = boat("treated_wood_boat", "Treated Wood Boat",
            () -> GTItems.TREATED_WOOD_BOAT.get());
    public static final EntityEntry<ChestBoat> TREATED_WOOD_CHEST_BOAT = chestBoat("treated_wood_chest_boat",
            "Treated Wood Boat with Chest", () -> GTItems.TREATED_WOOD_CHEST_BOAT.get());

    private static EntityEntry<Boat> boat(String name, String lang, Supplier<Item> drop) {
        return REGISTRATE.<Boat>entity(name, (type, level) -> new Boat(type, level, drop), MobCategory.MISC)
                .lang(lang)
                .properties(GTEntityTypes::boatProperties)
                .register();
    }

    private static EntityEntry<ChestBoat> chestBoat(String name, String lang, Supplier<Item> drop) {
        return REGISTRATE.<ChestBoat>entity(name, (type, level) -> new ChestBoat(type, level, drop), MobCategory.MISC)
                .lang(lang)
                .properties(GTEntityTypes::boatProperties)
                .register();
    }

    private static void boatProperties(EntityType.Builder<?> builder) {
        builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10);
    }

    public static void init() {}
}
