package brachy.modularui;

import brachy.modularui.screen.ModularContainerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.common.extensions.IMenuTypeExtension;
import com.gregtechceu.gtceu.compat.registries.DeferredHolder;
import com.gregtechceu.gtceu.compat.registries.DeferredRegister;

public class ModularUIMenuTypes {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ModularUI.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ModularContainerMenu>> MODULAR_CONTAINER = MENU_TYPES.register(
            "modular",
            () -> IMenuTypeExtension.create(ModularContainerMenu::new));

    public static void register(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
