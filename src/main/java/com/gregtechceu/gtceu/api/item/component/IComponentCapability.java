package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.Item;
import com.gregtechceu.gtceu.compat.capabilities.RegisterCapabilitiesEvent;

public interface IComponentCapability {

    void attachCapabilities(RegisterCapabilitiesEvent event, Item item);
}
