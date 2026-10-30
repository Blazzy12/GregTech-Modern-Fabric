package com.gregtechceu.gtceu.compat.client.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Item model properties registered per item, as before 26.x. Item model definitions read them through a
 * {@code range_dispatch} on {@code gtceu:legacy_property} with the property name.
 */
public final class ItemProperties {

    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("gtceu", "legacy_property");

    private static final Map<Item, Map<Identifier, ItemPropertyFunction>> PROPERTIES = new IdentityHashMap<>();
    private static final Map<Identifier, ItemPropertyFunction> GENERIC = new HashMap<>();

    private ItemProperties() {}

    public static void register(ItemLike item, Identifier name, ItemPropertyFunction function) {
        PROPERTIES.computeIfAbsent(item.asItem(), i -> new HashMap<>()).put(name, function);
    }

    public static void registerGeneric(Identifier name, ItemPropertyFunction function) {
        GENERIC.put(name, function);
    }

    public static @Nullable ItemPropertyFunction get(ItemStack stack, Identifier name) {
        var map = PROPERTIES.get(stack.getItem());
        ItemPropertyFunction function = map == null ? null : map.get(name);
        return function != null ? function : GENERIC.get(name);
    }

    public static void bootstrap() {
        RangeSelectItemModelProperties.ID_MAPPER.put(TYPE_ID, LegacyProperty.MAP_CODEC);
    }

    public record LegacyProperty(Identifier property) implements RangeSelectItemModelProperty {

        public static final MapCodec<LegacyProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("property").forGetter(LegacyProperty::property))
                .apply(i, LegacyProperty::new));

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            ItemPropertyFunction function = ItemProperties.get(stack, property);
            if (function == null) return 0;
            LivingEntity entity = owner != null ? owner.asLivingEntity() : null;
            return function.call(stack, level, entity, seed);
        }

        @Override
        public MapCodec<LegacyProperty> type() {
            return MAP_CODEC;
        }
    }
}
