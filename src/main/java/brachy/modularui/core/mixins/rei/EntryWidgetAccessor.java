package brachy.modularui.core.mixins.rei;

import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;

import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntryWidget.class)
public interface EntryWidgetAccessor {

    @Invoker(value = "drawHighlighted", remap = false)
    void modularui$invokeDrawHighlighted(GuiGraphics graphics, int mouseX, int mouseY, float delta);
}
