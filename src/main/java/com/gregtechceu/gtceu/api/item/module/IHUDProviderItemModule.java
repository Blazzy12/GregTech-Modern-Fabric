package com.gregtechceu.gtceu.api.item.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.ItemStack;

public interface IHUDProviderItemModule {

    @Environment(EnvType.CLIENT)
    default boolean shouldDrawHUD(ModuleContext moduleContext) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    void drawHUD(ModuleContext moduleContext, ArmorUtils.ModularHUD hud, GuiGraphicsExtractor graphics);

    @Environment(EnvType.CLIENT)
    static void tryDrawHUD(ItemStack stack, ArmorUtils.ModularHUD hud, GuiGraphicsExtractor graphics) {
        if (stack.isEmpty()) return;
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;
        for (ModuleContext module : modularItem.getAllModuleInstances()) {
            if (module.getModule() instanceof IHUDProviderItemModule hudProvider) {
                if (hudProvider.shouldDrawHUD(module)) hudProvider.drawHUD(module, hud, graphics);
                hud.newLine(CommonComponents.EMPTY);
            }
        }
    }
}
