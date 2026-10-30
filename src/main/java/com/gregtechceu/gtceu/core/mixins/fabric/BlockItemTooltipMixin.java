package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.IBlockTooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Blocks lost their tooltip hook in 26.x; block items call {@link IBlockTooltip} instead. */
@Mixin(BlockItem.class)
public abstract class BlockItemTooltipMixin extends Item {

    private BlockItemTooltipMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (((BlockItem) (Object) this).getBlock() instanceof IBlockTooltip tooltip) {
            List<Component> lines = new ArrayList<>();
            tooltip.appendHoverText(stack, context, lines, flag);
            lines.forEach(builder);
        }
    }
}
