package com.gregtechceu.gtceu.compat.client.event;

import com.gregtechceu.gtceu.compat.bus.Event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Fired on the client while an item tooltip is built, after the vanilla component lines. Fabric has no separate
 * attribute pass, so this is posted from the item tooltip callback.
 */
public class AddAttributeTooltipsEvent extends Event {

    protected final ItemStack stack;
    protected final Consumer<Component> tooltip;
    protected final Context ctx;

    public AddAttributeTooltipsEvent(ItemStack stack, Consumer<Component> tooltip, Context ctx) {
        this.stack = stack;
        this.tooltip = tooltip;
        this.ctx = ctx;
    }

    public Context getContext() {
        return this.ctx;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void addTooltipLines(Component... comps) {
        for (Component comp : comps) {
            this.tooltip.accept(comp);
        }
    }

    public record Context(Item.TooltipContext item, @Nullable Player player, TooltipFlag flag,
                          TooltipDisplay display) {}
}
