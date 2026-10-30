package com.gregtechceu.gtceu.common.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.common.block.LaserPipeBlock;

import com.gregtechceu.gtceu.compat.client.color.ItemColor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LaserPipeBlockItem extends PipeBlockItem {

    public LaserPipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public LaserPipeBlock getBlock() {
        return (LaserPipeBlock) super.getBlock();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof LaserPipeBlockItem materialBlockItem) {
                return LaserPipeBlock.tintedColor().getColor(materialBlockItem.getBlock().defaultBlockState(), null,
                        null, index);
            }
            return -1;
        };
    }
}
