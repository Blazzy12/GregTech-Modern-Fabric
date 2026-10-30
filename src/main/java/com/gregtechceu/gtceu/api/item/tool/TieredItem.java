package com.gregtechceu.gtceu.api.item.tool;

import net.minecraft.world.item.Item;

/** An item made from a {@link Tier}. Replaces the vanilla class removed in 26.x. */
public class TieredItem extends Item {

    private final Tier tier;

    public TieredItem(Tier tier, Properties properties) {
        super(properties.durability(tier.getUses()).enchantable(Math.max(1, tier.getEnchantmentValue())));
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }
}
