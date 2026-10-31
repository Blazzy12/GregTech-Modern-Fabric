package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * GT tools: crafting with a hammer gives the plate and the damaged hammer back, a GT pickaxe mines GT ores, and a
 * wrench turns a machine.
 */
public class GTToolsGameTest implements FabricClientGameTest {

    private static final BlockPos MACHINE = new BlockPos(-10, -60, 10);

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!GTTests.enabled("tools")) return;
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("setblock %d %d %d gtceu:lv_macerator[facing=north]".formatted(MACHINE.getX(),
                    MACHINE.getY(), MACHINE.getZ()));
            context.waitTicks(10);
            List<String> problems = server.computeOnServer(s -> check(s.overworld(),
                    s.getPlayerList().getPlayers().getFirst()));
            if (!problems.isEmpty()) throw new AssertionError("Tools: " + String.join("; ", problems));
        }
    }

    private static void wrench(ServerLevel level, ServerPlayer player, ItemStack wrench, Direction side) {
        var hit = new BlockHitResult(Vec3.atCenterOf(MACHINE).relative(side, 0.5), side, MACHINE, false);
        level.getBlockState(MACHINE).useItemOn(wrench, level, player, InteractionHand.MAIN_HAND, hit);
    }

    private static List<String> check(ServerLevel level, ServerPlayer player) {
        List<String> problems = new ArrayList<>();

        // h / I / I -> iron plate, the hammer comes back damaged
        ItemStack hammer = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron.value(), GTToolType.HARD_HAMMER)
                .asStack();
        var input = CraftingInput.of(1, 3, List.of(hammer, new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.IRON_INGOT)));
        var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isEmpty()) {
            problems.add("no hammer plate recipe");
        } else {
            ItemStack result = recipe.get().value().assemble(input);
            var remaining = recipe.get().value().getRemainingItems(input);
            GTCEu.LOGGER.info("[Tools] {} -> {} leaving {}", recipe.get().id(), result, remaining);
            if (!BuiltInRegistries.ITEM.getKey(result.getItem()).getPath().equals("iron_plate")) {
                problems.add("hammer recipe made " + result);
            }
            ItemStack left = remaining.getFirst();
            if (left.getItem() != hammer.getItem() || left.getDamageValue() <= 0) {
                problems.add("the hammer did not come back damaged: " + remaining);
            }
        }

        // a GT pickaxe is the right tool for GT ore, and faster than a hand
        ItemStack pickaxe = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron.value(), GTToolType.PICKAXE).asStack();
        var ore = BuiltInRegistries.BLOCK.getValue(GTCEu.id("tin_ore")).defaultBlockState();
        float speed = pickaxe.getDestroySpeed(ore);
        GTCEu.LOGGER.info("[Tools] iron pickaxe on tin ore: correct {}, speed {}", pickaxe.isCorrectToolForDrops(ore),
                speed);
        if (!pickaxe.isCorrectToolForDrops(ore)) problems.add("GT pickaxe can't harvest GT ore");
        if (speed <= 1) problems.add("GT pickaxe digs GT ore at speed " + speed);

        // a tool's innate enchantments count wherever enchantments are read (they come from the tool definition when
        // the enchantedTools option is on, or from the material; here the component is set directly)
        ItemStack knife = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron.value(), GTToolType.BUTCHERY_KNIFE)
                .asStack();
        var innate = new com.gregtechceu.gtceu.common.item.datacomponents.ResolvableItemEnchantments.Mutable();
        innate.upgrade(net.minecraft.world.item.enchantment.Enchantments.LOOTING, 3);
        knife.set(com.gregtechceu.gtceu.common.data.item.GTDataComponents.INNATE_ENCHANTMENTS.get(),
                innate.toImmutable());
        int looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), knife);
        GTCEu.LOGGER.info("[Tools] butchery knife with innate Looting III: looting {}", looting);
        if (looting != 3) problems.add("innate enchantments are ignored (looting " + looting + ")");

        // a wrench sets the machine's output side; sneaking with it turns the machine's front
        ItemStack wrench = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron.value(), GTToolType.WRENCH).asStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        var machine = MetaMachine.getMachine(level, MACHINE);
        player.setShiftKeyDown(false);
        wrench(level, player, wrench, Direction.EAST);
        var output = machine.getTrait(com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait.class);
        Direction outputSide = output == null ? null : output.getItemOutputDirection();
        player.setShiftKeyDown(true);
        wrench(level, player, wrench, Direction.SOUTH);
        player.setShiftKeyDown(false);
        Direction facing = machine.getFrontFacing();
        GTCEu.LOGGER.info("[Tools] wrench: output side {}, front after sneaking on south {}", outputSide, facing);
        if (outputSide != Direction.EAST) problems.add("wrench did not set the output side (" + outputSide + ")");
        if (facing != Direction.SOUTH) problems.add("sneaking with a wrench did not turn the machine (" + facing + ")");

        // using a cover item on a machine face places the cover (an onItemUseFirst behaviour), through the same
        // server-side path a real click takes
        ItemStack conveyor = new ItemStack(BuiltInRegistries.ITEM.getValue(GTCEu.id("lv_conveyor_module")));
        player.setItemInHand(InteractionHand.MAIN_HAND, conveyor);
        // stand two blocks west of the machine, looking at it
        player.snapTo(MACHINE.getX() - 1.5, MACHINE.getY(), MACHINE.getZ() + 0.5, -90, 29);
        var hit = new BlockHitResult(Vec3.atCenterOf(MACHINE).relative(Direction.WEST, 0.5), Direction.WEST,
                MACHINE, false);
        // the cover goes on the side the player looks at
        Direction side = com.gregtechceu.gtceu.api.capability.ICoverable.rayTraceCoverableSide(
                machine.getCoverContainer(), player);
        player.gameMode.useItemOn(player, level, conveyor, InteractionHand.MAIN_HAND, hit);
        var cover = side == null ? null : machine.getCoverContainer().getCoverAtSide(side);
        GTCEu.LOGGER.info("[Tools] conveyor used on the machine's {} side: cover {}", side, cover);
        if (cover == null) problems.add("using a conveyor module on a machine did not place the cover");
        return problems;
    }
}
