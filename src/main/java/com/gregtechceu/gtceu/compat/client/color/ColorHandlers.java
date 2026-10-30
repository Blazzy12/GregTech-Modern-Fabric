package com.gregtechceu.gtceu.compat.client.color;

import net.minecraft.core.BlockPos;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Bridges legacy color handlers to 26.x tinting. Block colors become a Fabric tints factory with
 * {@link #BLOCK_TINT_LAYERS} layers. Item colors are looked up by the {@code gtceu:legacy_color} tint source,
 * which item model definitions reference with the tint index.
 */
public final class ColorHandlers {

    public static final int BLOCK_TINT_LAYERS = 4;
    public static final Identifier LEGACY_ITEM_TINT = Identifier.fromNamespaceAndPath("gtceu", "legacy_color");

    private static final Map<Item, ItemColor> ITEM_COLORS = new IdentityHashMap<>();

    private ColorHandlers() {}

    public static void registerBlock(BlockColor color, Block... blocks) {
        BlockColorRegistry.register((state, level, pos, out) -> {
            for (int i = 0; i < BLOCK_TINT_LAYERS; i++) out.add(opaque(color.getColor(state, level, pos, i)));
        }, blocks);
    }

    public static void registerItem(ItemColor color, ItemLike... items) {
        for (ItemLike item : items) ITEM_COLORS.put(item.asItem(), color);
    }

    public static @Nullable ItemColor getItemColor(Item item) {
        return ITEM_COLORS.get(item);
    }

    public static boolean hasItemColor(Item item) {
        return ITEM_COLORS.containsKey(item);
    }

    /**
     * @return the tint colour of a block's tint index, like the removed {@code BlockColors#getColor}, or -1
     */
    public static int getBlockColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                    int tintIndex) {
        var source = net.minecraft.client.Minecraft.getInstance().getBlockColors().getTintSource(state, tintIndex);
        if (source == null) return -1;
        return level != null && pos != null ? source.colorInWorld(state, level, pos) : source.color(state);
    }

    public static void bootstrap() {
        ItemTintSources.ID_MAPPER.put(LEGACY_ITEM_TINT, LegacyItemTint.MAP_CODEC);
    }

    private static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? ARGB.opaque(color) : color;
    }

    /** Item tint source that defers to the {@link ItemColor} registered for the item. */
    public record LegacyItemTint(int index) implements ItemTintSource {

        public static final MapCodec<LegacyItemTint> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(LegacyItemTint::index))
                        .apply(i, LegacyItemTint::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            ItemColor color = ITEM_COLORS.get(stack.getItem());
            return color == null ? -1 : opaque(color.getColor(stack, index));
        }

        @Override
        public MapCodec<LegacyItemTint> type() {
            return MAP_CODEC;
        }
    }
}
