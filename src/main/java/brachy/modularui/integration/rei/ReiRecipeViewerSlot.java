package brachy.modularui.integration.rei;

import brachy.modularui.utils.MouseButtons;

import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

import brachy.modularui.core.mixins.rei.EntryWidgetAccessor;
import brachy.modularui.drawable.TooltipComponentIcon;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;

import brachy.modularui.theme.WidgetThemeEntry;

import lombok.Getter;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReiRecipeViewerSlot<I> extends RecipeViewerSlotWidget<I, ReiRecipeViewerSlot<I>> {

    private static final Point ONE = new Point(1, 1);

    @Getter private Slot slotWidget;

    public ReiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);
        this.slotWidget = new EntryWidget(ONE).disableBackground().disableTooltips();

        this.tooltipAutoUpdate(true);
        this.tooltipDynamic(r -> {
            if (slotWidget != null) {
                Tooltip tooltip = slotWidget.getCurrentTooltip(TooltipContext.ofMouse(Item.TooltipContext.of(Minecraft.getInstance().level)));
                if (tooltip == null) return;
                tooltip.entries().forEach(e -> {
                    if (e.isText()) {
                        r.addLine(e.getAsText());
                    } else if (e.isTooltipComponent()) {
                        r.addDrawableLine(new TooltipComponentIcon(e.getAsTooltipComponent(), false));
                    }
                });
            }
        });
        size(18, 18);
    }

    @Override
    protected void rebuildRealSlot() {
        slotWidget = new EntryWidget(ONE).disableBackground().disableTooltips();
        slotWidget.entries(REIStackConverter.convertToReiEntry(this.entries, this.chance, this.renderMappingFunction));

        if (recipeSlotRole == RecipeSlotRole.INPUT || recipeSlotRole == RecipeSlotRole.CATALYST) {
            slotWidget.markInput();
        } else if (recipeSlotRole == RecipeSlotRole.OUTPUT) {
            slotWidget.markOutput();
        } else {
            slotWidget.unmarkInputOrOutput();
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = GuiGraphics.of(context.getGraphics());
        this.slotWidget.render(graphics, context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        if (this.isHovering() && slotWidget instanceof EntryWidgetAccessor accessor){
            accessor.modularui$invokeDrawHighlighted(graphics, context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        }
    }

    @Override
    public Result onMousePressed(int button) {
        var event = new MouseButtonEvent(getContext().getMouseX(), getContext().getMouseY(),
                new MouseButtonInfo(MouseButtons.fromLegacy(button), 0));
        return this.slotWidget.mouseClicked(event, false) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(new KeyEvent(keyCode, scanCode, modifiers)) ? Result.SUCCESS : Result.ACCEPT;
    }
}
