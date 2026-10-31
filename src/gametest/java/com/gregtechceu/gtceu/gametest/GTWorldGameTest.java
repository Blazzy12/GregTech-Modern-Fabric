package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.common.block.SurfaceRockBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A normal overworld gets GT's worldgen: ore veins with ore blocks that drop items, surface rock indicators, and
 * bedrock fluid veins.
 */
public class GTWorldGameTest implements FabricClientGameTest {

    private static final int RADIUS = 5;

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!GTTests.enabled("world")) return;
        try (TestSingleplayerContext world = context.worldBuilder()
                .setUseConsistentSettings(false)
                .adjustSettings(settings -> {
                    settings.setWorldType(settings.getNormalPresetList().getFirst());
                    settings.setSeed("1234567");
                })
                .create()) {
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("gtceu_world_normal");

            List<String> problems = world.getServer().computeOnServer(s -> check(s.overworld()));
            if (!problems.isEmpty()) throw new AssertionError("Worldgen: " + String.join("; ", problems));
        }
    }

    private static List<String> check(ServerLevel level) {
        List<String> problems = new ArrayList<>();
        BlockPos spawn = level.getRespawnData().pos();
        int cx0 = spawn.getX() >> 4, cz0 = spawn.getZ() >> 4;
        Map<String, Integer> ores = new TreeMap<>();
        int rocks = 0;
        BlockState anOre = null;
        BlockPos anOrePos = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = cx0 - RADIUS; cx <= cx0 + RADIUS; cx++) {
            for (int cz = cz0 - RADIUS; cz <= cz0 + RADIUS; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (int y = level.getMinY(); y < level.getMaxY(); y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            pos.set(cx * 16 + x, y, cz * 16 + z);
                            BlockState state = chunk.getBlockState(pos);
                            Block block = state.getBlock();
                            if (block instanceof OreBlock) {
                                ores.merge(BuiltInRegistries.BLOCK.getKey(block).getPath(), 1, Integer::sum);
                                if (anOre == null) {
                                    anOre = state;
                                    anOrePos = pos.immutable();
                                }
                            } else if (block instanceof SurfaceRockBlock) {
                                rocks++;
                            }
                        }
                    }
                }
            }
        }
        int total = ores.values().stream().mapToInt(Integer::intValue).sum();
        GTCEu.LOGGER.info("[World check] {} GT ore blocks in {} kinds, {} surface rocks around {}", total,
                ores.size(), rocks, spawn);
        ores.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(12)
                .forEach(e -> GTCEu.LOGGER.info("[World check]   {} x{}", e.getKey(), e.getValue()));
        if (total == 0) problems.add("no GT ores generated");
        if (rocks == 0) problems.add("no surface rocks generated");

        if (anOre != null) {
            List<ItemStack> drops = Block.getDrops(anOre, level, anOrePos, null, null,
                    new ItemStack(Items.DIAMOND_PICKAXE));
            GTCEu.LOGGER.info("[World check] {} drops {}", BuiltInRegistries.BLOCK.getKey(anOre.getBlock()), drops);
            if (drops.isEmpty()) problems.add(BuiltInRegistries.BLOCK.getKey(anOre.getBlock()) + " drops nothing");
        }

        var fluids = BedrockFluidVeinSavedData.getOrCreate(level);
        Map<String, Integer> veins = new TreeMap<>();
        for (int cx = cx0 - 16; cx <= cx0 + 16; cx += 4) {
            for (int cz = cz0 - 16; cz <= cz0 + 16; cz += 4) {
                var fluid = fluids.getFluidInChunk(cx, cz);
                String id = fluid == null || fluid == Fluids.EMPTY ? "none" : BuiltInRegistries.FLUID.getKey(fluid).toString();
                veins.merge(id, 1, Integer::sum);
            }
        }
        GTCEu.LOGGER.info("[World check] bedrock fluid veins sampled: {}", veins);
        if (veins.keySet().stream().allMatch("none"::equals)) problems.add("no bedrock fluid veins");
        return problems;
    }
}
