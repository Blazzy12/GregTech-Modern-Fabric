package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Fired from Fabric's creative tab output event for every tab. */
public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {

    private final CreativeModeTab tab;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final FabricCreativeModeTabOutput entries;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey,
                                             FabricCreativeModeTabOutput entries) {
        this.tab = tab;
        this.tabKey = tabKey;
        this.entries = entries;
    }

    public CreativeModeTab getTab() {
        return tab;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return tabKey;
    }

    public FeatureFlagSet getFlags() {
        return entries.getEnabledFeatures();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return entries.getContext();
    }

    public boolean hasPermissions() {
        return entries.shouldShowOpRestrictedItems();
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        entries.accept(stack, visibility);
    }

    public void accept(ItemLike item) {
        accept(new ItemStack(item));
    }
}
