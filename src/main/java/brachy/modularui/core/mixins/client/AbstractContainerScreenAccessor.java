package brachy.modularui.core.mixins.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Mutable
    @Accessor
    void setImageWidth(int v);

    @Mutable
    @Accessor
    void setImageHeight(int v);

    @Accessor
    void setLeftPos(int v);

    @Accessor
    int getLeftPos();

    @Accessor
    int getTopPos();

    @Accessor
    void setTopPos(int v);

    @Accessor
    void setHoveredSlot(Slot slot);

    @Accessor
    Slot getHoveredSlot();

    @Accessor
    boolean getIsQuickCrafting();

    @Accessor
    Set<Slot> getQuickCraftSlots();

    @Accessor
    int getQuickCraftingType();

    @Invoker
    void invokeRecalculateQuickCraftRemaining();

    @Accessor
    int getQuickCraftingRemainder();

    @Invoker
    void invokeExtractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY);
}
