package com.gregtechceu.gtceu.compat.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/** The subset of the shared "c" convention tags that GregTech uses. */
public final class Tags {

    private Tags() {}

    public static final class Biomes {

        private Biomes() {}

        private static TagKey<net.minecraft.world.level.biome.Biome> tag(String path) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", path));
        }

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_COLD = tag("is_cold");

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_PLAINS = tag("is_plains");

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_SANDY = tag("is_sandy");

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_SNOWY = tag("is_snowy");

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_SWAMP = tag("is_swamp");

        public static final TagKey<net.minecraft.world.level.biome.Biome> IS_WET = tag("is_wet");
    }

    public static final class Blocks {

        private Blocks() {}

        private static TagKey<net.minecraft.world.level.block.Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
        }

        public static final TagKey<net.minecraft.world.level.block.Block> COBBLESTONES = tag("cobblestones");

        public static final TagKey<net.minecraft.world.level.block.Block> CONCRETES = tag("concretes");

        public static final TagKey<net.minecraft.world.level.block.Block> END_STONES = tag("end_stones");

        public static final TagKey<net.minecraft.world.level.block.Block> GLASS_BLOCKS = tag("glass_blocks");

        public static final TagKey<net.minecraft.world.level.block.Block> GLASS_PANES = tag("glass_panes");

        public static final TagKey<net.minecraft.world.level.block.Block> NEEDS_NETHERITE_TOOL = net.minecraft.tags.TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("neoforge", "needs_netherite_tool"));

        public static final TagKey<net.minecraft.world.level.block.Block> NEEDS_WOOD_TOOL = net.minecraft.tags.TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("neoforge", "needs_wood_tool"));

        public static final TagKey<net.minecraft.world.level.block.Block> ORES = tag("ores");

        public static final TagKey<net.minecraft.world.level.block.Block> SANDS = tag("sands");

        public static final TagKey<net.minecraft.world.level.block.Block> STONES = tag("stones");
    }

    public static final class DamageTypes {

        private DamageTypes() {}

        private static TagKey<net.minecraft.world.damagesource.DamageType> tag(String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("c", path));
        }

        public static final TagKey<net.minecraft.world.damagesource.DamageType> IS_ENVIRONMENT = tag("is_environment");

        public static final TagKey<net.minecraft.world.damagesource.DamageType> IS_PHYSICAL = tag("is_physical");

        public static final TagKey<net.minecraft.world.damagesource.DamageType> IS_POISON = tag("is_poison");
    }

    public static final class Fluids {

        private Fluids() {}

        private static TagKey<net.minecraft.world.level.material.Fluid> tag(String path) {
            return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", path));
        }

        public static final TagKey<net.minecraft.world.level.material.Fluid> GASEOUS = tag("gaseous");
    }

    public static final class Items {

        private Items() {}

        private static TagKey<net.minecraft.world.item.Item> tag(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }

        public static final TagKey<net.minecraft.world.item.Item> CHESTS_WOODEN = tag("chests/wooden");

        public static final TagKey<net.minecraft.world.item.Item> CONCRETE_POWDERS = tag("concrete_powders");

        public static final TagKey<net.minecraft.world.item.Item> CONCRETES = tag("concretes");

        public static final TagKey<net.minecraft.world.item.Item> DYED = tag("dyed");

        public static final TagKey<net.minecraft.world.item.Item> DYES = tag("dyes");

        public static final TagKey<net.minecraft.world.item.Item> DYES_BLACK = tag("dyes/black");

        public static final TagKey<net.minecraft.world.item.Item> DYES_YELLOW = tag("dyes/yellow");

        public static final TagKey<net.minecraft.world.item.Item> ENCHANTING_FUELS = tag("enchanting_fuels");

        public static final TagKey<net.minecraft.world.item.Item> GLASS_BLOCKS = tag("glass_blocks");

        public static final TagKey<net.minecraft.world.item.Item> GLASS_PANES = tag("glass_panes");

        public static final TagKey<net.minecraft.world.item.Item> GRAVELS = tag("gravels");

        public static final TagKey<net.minecraft.world.item.Item> RODS_WOODEN = tag("rods/wooden");

        public static final TagKey<net.minecraft.world.item.Item> SANDS = tag("sands");

        public static final TagKey<net.minecraft.world.item.Item> SEEDS = tag("seeds");

        public static final TagKey<net.minecraft.world.item.Item> TOOLS_IGNITER = tag("tools/igniter");

        public static final TagKey<net.minecraft.world.item.Item> TOOLS_SHEAR = tag("tools/shear");

        public static final TagKey<net.minecraft.world.item.Item> TOOLS_WRENCH = tag("tools/wrench");
    }
}
