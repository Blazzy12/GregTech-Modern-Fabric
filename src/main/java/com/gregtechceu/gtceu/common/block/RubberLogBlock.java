package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.gregtechceu.gtceu.compat.common.ItemAbilities;
import com.gregtechceu.gtceu.compat.common.ItemAbility;

import org.jetbrains.annotations.Nullable;

public class RubberLogBlock extends RotatedPillarBlock {

    public static final BooleanProperty NATURAL = GTBlockStateProperties.NATURAL;

    public RubberLogBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NATURAL);
    }

    public boolean isNatural(BlockState state) {
        return state.getOptionalValue(NATURAL).orElse(false);
    }

    public BlockState changeNatural(BlockState state, boolean natural) {
        if (state.is(this)) {
            return state.setValue(NATURAL, natural);
        }
        return state;
    }
}
