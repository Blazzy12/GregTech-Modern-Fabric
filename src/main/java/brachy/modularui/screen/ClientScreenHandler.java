package brachy.modularui.screen;

import brachy.modularui.GuiErrorHandler;
import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.ITheme;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.UIType;
import brachy.modularui.api.widget.IVanillaSlot;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.core.mixins.client.AbstractContainerScreenAccessor;
import brachy.modularui.core.mixins.client.ScreenAccessor;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.text.FontRenderHelper;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.LocatedWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.FpsCounter;
import brachy.modularui.utils.Stencil;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.RichTextWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import brachy.modularui.utils.MouseButtons;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.gregtechceu.gtceu.compat.dist.Dist;
import com.gregtechceu.gtceu.compat.bus.EventPriority;
import com.gregtechceu.gtceu.compat.bus.SubscribeEvent;
import com.gregtechceu.gtceu.compat.fml.common.EventBusSubscriber;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientItemExtensions;
import com.gregtechceu.gtceu.compat.common.NeoForge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Internal
@EventBusSubscriber(modid = ModularUI.MOD_ID, value = Dist.CLIENT)
public class ClientScreenHandler {

    @Getter
    private static final GuiContext defaultContext = new GuiContext(UIType.NONE);
    private static final FpsCounter fpsCounter = new FpsCounter();
    private static final int DEFAULT_DEBUG_TEXT_COLOR = 0xFFAAAAAA;
    private static final int DEFAULT_DEBUG_OUTLINE_COLOR = 0xDCB42873;

    private static ModularScreen currentScreen = null;
    @Getter
    private static long ticks = 0L;
    private static IMuiScreen lastMui;
    private static final ObjectArrayList<IMuiScreen> muiStack = new ObjectArrayList<>(8);

    private static boolean debugToggleActive = false;

    /**
     * Hooks MUI into every screen through Fabric's screen events (NeoForge's {@code ScreenEvent}s before 26.x).
     * Called once from the client entrypoint; screen changes come from {@code MinecraftSetScreenMixin}.
     */
    public static void registerEvents() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            onInitScreenPost(screen);
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) ->
                    !onScreenKeyPressed(s, event.key(), event.keycode(), event.modifiers()));
            ScreenKeyboardEvents.allowKeyRelease(screen).register((s, event) ->
                    !onScreenKeyReleased(s, event.key(), event.keycode(), event.modifiers()));
            ScreenKeyboardEvents.allowCharType(screen).register((s, event) ->
                    !onScreenCharTyped(s, (char) event.codepoint(), 0));
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
                    !onScreenMousePressed(s, MouseButtons.toLegacy(event.button())));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, event) ->
                    !onScreenMouseReleased(s, MouseButtons.toLegacy(event.button())));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, x, y, scrollX, scrollY) ->
                    !onScreenMouseScrolled(s, scrollX, scrollY));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, event, dx, dy) ->
                    !onScreenMouseDragged(s, MouseButtons.toLegacy(event.button()), dx, dy));
            ScreenEvents.afterExtract(screen).register(ClientScreenHandler::onScreenRenderPost);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
    }

    // we need to know the actual gui and not some fake screen some other mod overwrites
    public static void onOpenScreen(@Nullable Screen currentScreen, @Nullable Screen newScreen) {
        onGuiChanged(currentScreen, newScreen);
    }

    public static void onCloseScreens(Screen closing) {
        // called when the next screen is null, so that the player returns to the world
        // we cant use ScreenEvent.Closing since that's also called when transitioning screens
        onGuiChanged(closing, null);
    }

    public static void onInitScreenPost(Screen screen) {
        defaultContext.updateScreenArea(screen.width, screen.height);
        if (validateGui(screen)) {
            currentScreen.onResize(screen.width, screen.height);
        }
        OverlayStack.foreach(ms -> ms.onResize(screen.width, screen.height), false);
    }

    /** @return whether MUI handled the key and vanilla should not see it */
    public static boolean onScreenKeyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        defaultContext.updateKey(keyCode, scanCode, modifiers, true);
        if (validateGui(screen)) {
            currentScreen.getContext().updateKey(keyCode, scanCode, modifiers, true);
        }
        if (handleKeyboardInput(currentScreen, screen, true, InputPhase.EARLY, keyCode, scanCode, modifiers)) {
            return true;
        }
        return handleKeyboardInput(currentScreen, screen, true, InputPhase.LATE, keyCode, scanCode, modifiers);
    }

    public static boolean onScreenKeyReleased(Screen screen, int keyCode, int scanCode, int modifiers) {
        defaultContext.updateKey(keyCode, scanCode, modifiers, false);
        if (validateGui(screen)) {
            currentScreen.getContext().updateKey(keyCode, scanCode, modifiers, false);
        }
        // dont need late for release event
        return handleKeyboardInput(currentScreen, screen, false, InputPhase.EARLY, keyCode, scanCode, modifiers);
    }

    public static boolean onScreenCharTyped(Screen screen, char codePoint, int modifiers) {
        defaultContext.updateTypedChar(codePoint, modifiers);
        if (validateGui(screen)) currentScreen.getContext().updateTypedChar(codePoint, modifiers);

        // vanilla also casts to char here
        return doAction(currentScreen, ms -> ms.charTyped(codePoint, modifiers));
    }

    public static boolean onScreenMousePressed(Screen screen, int button) {
        defaultContext.updateMouseButton(button, true);
        if (validateGui(screen)) currentScreen.getContext().updateMouseButton(button, true);

        if (button == -1) {
            return false;
        }
        if (currentScreen != null && currentScreen.handleDraggableInput(button, true) ||
                doAction(currentScreen, ms -> ms.mousePressed(button))) {
            RecipeViewerHandler.getCurrent().setSearchFocused(false);
            return true;
        }
        return false;
    }

    // Recipe viewers must finish an external drag before MUI handles the release over the target widget. Otherwise
    // MUI cancels the event and JEI never gets a chance to deliver the ghost ingredient.
    public static boolean onScreenMouseReleased(Screen screen, int button) {
        defaultContext.updateMouseButton(button, false);
        if (validateGui(screen)) currentScreen.getContext().updateMouseButton(button, false);

        if (currentScreen != null && currentScreen.handleDraggableInput(button, false) ||
                doAction(currentScreen, ms -> ms.mouseReleased(button))) {
            RecipeViewerHandler.getCurrent().setSearchFocused(false);
            return true;
        }
        return false;
    }

    public static boolean onScreenMouseScrolled(Screen screen, double wx, double wy) {
        if (wx == 0 && wy == 0) return false;
        defaultContext.updateMouseWheel(wx, wy);
        if (validateGui(screen)) currentScreen.getContext().updateMouseWheel(wx, wy);

        return doAction(currentScreen, ms -> ms.mouseScrolled(wx, wy));
    }

    public static boolean onScreenMouseDragged(Screen screen, int button, double dragX, double dragY) {
        return doAction(currentScreen, ms -> ms.mouseDragged(button, dragX, dragY));
    }

    /**
     * Draws a MUI screen in place of its wrapper's vanilla rendering.
     *
     * @return whether the screen was a valid MUI screen and was drawn
     */
    public static boolean onScreenRender(Screen screen, GuiGraphicsExtractor gc, int mx, int my, float pt) {
        defaultContext.setGraphics(gc);
        defaultContext.updateState(mx, my, pt);
        defaultContext.reset();
        if (validateGui(screen)) {
            currentScreen.getContext().setGraphics(gc);
            currentScreen.getContext().updateState(mx, my, pt);
            drawScreen(gc, currentScreen, currentScreen.getScreenWrapper().wrappedScreen(), mx, my, pt);
            brachy.modularui.utils.CursorHandler.apply(gc);
            return true;
        }
        return false;
    }

    public static void onScreenRenderPost(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                          float partialTick) {
        OverlayStack.draw(graphics, mouseX, mouseY, partialTick);
    }

    public static void onClientTick() {
        OverlayStack.onTick();
        defaultContext.tick();
        if (validateGui()) {
            currentScreen.onUpdate();
        }
        ticks++;
    }

    public static void onFrameUpdate() {
        OverlayStack.foreach(ModularScreen::onFrameUpdate, true);
        if (currentScreen != null) currentScreen.onFrameUpdate();
    }

    private static void onGuiChanged(Screen oldScreen, Screen newScreen) {
        if (oldScreen == newScreen) return;
        defaultContext.reset();
        fpsCounter.reset();
        GuiErrorHandler.INSTANCE.clear();

        IMuiScreen lastLastMui = lastMui;
        if (lastMui != null) {
            // called on open and close
            // invalidate last mui screen, but keep it in stack
            invalidateCurrentScreen();
        }

        if (newScreen instanceof IMuiScreen muiScreen) {
            lastMui = muiScreen;
            currentScreen = muiScreen.screen();
            muiStack.remove(muiScreen);
            muiStack.add(muiScreen); // move screen to the top of the stack;
            Screen lastParent = lastLastMui != null ? lastLastMui.screen().getContext().getParent() : null;
            if (lastParent != muiScreen) {
                // new screen in the stack
                currentScreen.getContext().setParent(oldScreen);
            } else {
                // last parent is equal to new screen -> effectively popping the current screen from the stack
                // the current screen will disconnect from the stack and therefore need to dispose it
                muiStack.remove(lastLastMui);
                lastLastMui.screen().getPanelManager().dispose();
            }
        } else if (newScreen == null) {
            // closing -> clear stack and dispose every screen
            invalidateMuiStack();
            // only when all screens are closed dispose all containers in the stack
            ModularNetwork.CLIENT.closeAll();
        }

        OverlayStack.onOpenScreen(newScreen);
    }

    private static void invalidateCurrentScreen() {
        // reset mouse inputs, relevant when screen gets reopened
        if (lastMui != null) {
            lastMui.screen().getPanelManager().closeScreen();
            lastMui = null;
        }
        currentScreen = null;
    }

    private static void invalidateMuiStack() {
        muiStack.forEach(muiScreen -> muiScreen.screen().getPanelManager().dispose());
        muiStack.clear();
    }

    private static boolean doAction(@Nullable ModularScreen muiScreen, Predicate<ModularScreen> action) {
        return OverlayStack.interact(action, true) || (muiScreen != null && action.test(muiScreen));
    }

    private static void foreach(@Nullable ModularScreen muiScreen, Consumer<ModularScreen> action) {
        OverlayStack.foreach(action, true);
        if (muiScreen != null) action.accept(muiScreen);
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    private static boolean handleKeyboardInput(@Nullable ModularScreen muiScreen, Screen mcScreen,
                                               boolean isPress, InputPhase inputPhase,
                                               int keyCode, int scanCode, int modifiers) {
        if (isPress) {
            // pressing a key
            return inputPhase.isEarly() ? doAction(muiScreen, ms -> ms.keyPressed(keyCode, scanCode, modifiers)) :
                    keyTyped(mcScreen, keyCode, scanCode, modifiers);
        } else {
            // releasing a key
            return inputPhase.isEarly() && doAction(muiScreen, ms -> ms.keyReleased(keyCode, scanCode, modifiers));
        }
    }

    private static boolean keyTyped(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (currentScreen == null) return false;
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == InputConstants.KEY_C && Interactable.isControl(modifiers) && Interactable.isShift(modifiers) &&
                Interactable.isAlt(modifiers)) {
            if (!debugToggleActive) {
                ModularUIConfig.DEBUG_UI.set(!ModularUIConfig.Dev.debugUI());
                debugToggleActive = true;
            }
            return true;
        } else {
            debugToggleActive = false;
        }
        boolean hasLevel = Minecraft.getInstance().level != null;
        boolean closeOnEsc = screen.shouldCloseOnEsc();
        if (keyCode == InputConstants.KEY_ESCAPE && (!hasLevel || closeOnEsc)) {
            if (hasLevel) {
                // close everything in world
                if (currentScreen.getContext().hasDraggable()) {
                    currentScreen.getContext().dropDraggable(true);
                }
                currentScreen.getPanelManager().closePanelsAndScreen();
            } else if (closeOnEsc || !currentScreen.getPanelManager().getTopMostPanel().isMainPanel()) {
                // close top panel if screen can be close or the top panel is not a main panel
                dropOrClosePanel();
            }
            return true;
        }
        if (!hasLevel) return false; // E only closes in world
        if (Minecraft.getInstance().options.keyInventory
                .matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && !RecipeViewerHandler.getCurrent().isSearchFocused()) {
            dropOrClosePanel();
            return true;
        }
        return false;
    }

    private static void dropOrClosePanel() {
        if (currentScreen.getContext().hasDraggable()) {
            currentScreen.getContext().dropDraggable(true);
        } else {
            currentScreen.getPanelManager().closeTopPanel();
        }
    }

    private static MouseButtonEvent mouseEvent(ModularGuiContext ctx, int button) {
        return new MouseButtonEvent(ctx.getMouseX(), ctx.getMouseY(), new MouseButtonInfo(MouseButtons.fromLegacy(button), 0));
    }

    public static void dragSlot(int button, double dragX, double dragY) {
        ModularGuiContext ctx = currentScreen.getContext();
        getMCScreen().mouseDragged(mouseEvent(ctx, button), dragX, dragY);
    }

    public static void clickSlot(ModularScreen ms, Slot slot) {
        Screen screen = ms.getScreenWrapper().wrappedScreen();
        if (screen instanceof ScreenAccessor acc && screen instanceof IClickableContainerScreen clickableScreen &&
                validateGui(screen)) {
            ModularGuiContext ctx = ms.getContext();
            var buttonList = screen.children();
            try {
                // remove buttons to make sure they are not clicked
                acc.setChildren(Collections.emptyList());
                // set clicked slot to make sure the container clicks the desired slot
                clickableScreen.modularui$setClickedSlot(slot);
                screen.mouseClicked(mouseEvent(ctx, ctx.getLastMouseButton()), false);
            } finally {
                // undo modifications
                clickableScreen.modularui$setClickedSlot(null);
                acc.setChildren(buttonList);
            }
        }
    }

    public static void releaseSlot() {
        if (hasScreen() && getMCScreen() != null) {
            ModularGuiContext ctx = currentScreen.getContext();
            getMCScreen().mouseReleased(mouseEvent(ctx, ctx.getLastMouseButton()));
        }
    }

    public static boolean shouldDrawWorldBackground() {
        return /* ModularUI.isBlurLoaded() || */Minecraft.getInstance().level == null;
    }

    public static void drawDarkBackground(Screen screen, GuiGraphicsExtractor guiGraphics) {
        if (hasScreen()) {
            float alpha = currentScreen.getMainPanel().getAlpha();
            // vanilla color values as hex
            int color = 0x101010;
            int startAlpha = 0xc0;
            int endAlpha = 0xd0;
            guiGraphics.fillGradient(0, 0, screen.width, screen.height,
                    Color.withAlpha(color, (int) (startAlpha * alpha)),
                    Color.withAlpha(color, (int) (endAlpha * alpha)));
        }
    }

    public static void drawScreen(GuiGraphicsExtractor graphics, ModularScreen muiScreen, Screen mcScreen,
                                  int mouseX, int mouseY, float partialTicks) {
        if (mcScreen instanceof AbstractContainerScreen<?> container) {
            drawContainer(graphics, muiScreen, container, mouseX, mouseY, partialTicks);
        } else {
            drawScreenInternal(graphics, muiScreen, mcScreen, mouseX, mouseY, partialTicks);
        }
    }

    public static void drawScreenInternal(GuiGraphicsExtractor graphics, ModularScreen muiScreen, Screen mcScreen,
                                          int mouseX, int mouseY, float partialTicks) {
        Stencil.reset();
        muiScreen.getContext().reset();
        muiScreen.getContext().getStencil().push(muiScreen.getScreenArea());
        muiScreen.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        drawVanillaElements(graphics, mcScreen, mouseX, mouseY, partialTicks);
        GuiDraw.resetColor();
        muiScreen.drawForeground(graphics);
        muiScreen.getContext().getStencil().pop();
    }

    /**
     * Draws a MUI container screen. 26.x container screens keep less drag state; the carried item is drawn by
     * {@link AbstractContainerScreen#extractCarriedItem}.
     */
    public static void drawContainer(GuiGraphicsExtractor graphics, ModularScreen muiScreen,
                                     AbstractContainerScreen<?> mcScreen,
                                     int mouseX, int mouseY, float partialTicks) {
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) mcScreen;

        Stencil.reset();
        muiScreen.getContext().reset();
        muiScreen.getContext().getStencil().push(muiScreen.getScreenArea());
        mcScreen.extractBackground(graphics, mouseX, mouseY, partialTicks);

        muiScreen.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        // mainly for invtweaks compat
        drawVanillaElements(graphics, mcScreen, mouseX, mouseY, partialTicks);
        GuiDraw.resetColor();

        acc.setHoveredSlot(null);
        IWidget hovered = muiScreen.getContext().getTopHovered();
        if (hovered instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
            acc.setHoveredSlot(vanillaSlot.getVanillaSlot());
        }

        mcScreen.extractCarriedItem(graphics, mouseX, mouseY);

        muiScreen.drawForeground(graphics);
        muiScreen.getContext().getStencil().pop();
    }

    @ApiStatus.Internal
    public static void drawVanillaElements(GuiGraphicsExtractor graphics, Screen mcScreen, int mouseX, int mouseY,
                                           float partialTicks) {
        drawVanillaElements(graphics, mcScreen, mouseX, mouseY, partialTicks, r -> true);
    }

    @ApiStatus.Internal
    public static void drawVanillaElements(GuiGraphicsExtractor graphics, Screen mcScreen, int mouseX, int mouseY,
                                           float partialTicks, Predicate<Renderable> filter) {
        for (Renderable renderable : ((ScreenAccessor) mcScreen).getRenderables()) {
            if (filter.test(renderable)) {
                renderable.extractRenderState(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    public static void drawDebugScreen(GuiGraphicsExtractor graphics, @Nullable ModularScreen muiScreen, @Nullable ModularScreen fallback) {
        fpsCounter.onDraw();
        if (!ModularUIConfig.Dev.debugUI()) return;
        if (muiScreen == null) {
            if (validateGui()) {
                muiScreen = currentScreen;
            } else {
                if (fallback == null) return;
                muiScreen = fallback;
            }
        }
        ModularGuiContext context = muiScreen.getContext();

        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenH = muiScreen.getScreenArea().height;
        int outlineColor = Color.parseString(ModularUIConfig.DEBUG_OUTLINE_COLOR.get(), DEFAULT_DEBUG_OUTLINE_COLOR).resultOrPartial(s -> {}).orElseThrow();
        int textColor = Color.parseString(ModularUIConfig.DEBUG_TEXT_COLOR.get(), DEFAULT_DEBUG_TEXT_COLOR).resultOrPartial(s -> {}).orElseThrow();
        float scale = ModularUIConfig.Dev.scale();
        int shift = (int) (11 * scale + 0.5f);
        int lineY = screenH - shift - 2;
        if (ModularUI.Mods.isRecipeViewerLoaded() &&
                muiScreen.getContext().hasSettings() &&
                muiScreen.getContext().getRecipeViewerSettings().isEnabled(muiScreen)) {
            lineY -= 18;
        }

        String s = I18n.get("modularui.debug.mouse_pos", mouseX, mouseY);
        GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
        lineY -= shift;
        Component c = Component.translatable("modularui.debug.fps", fpsCounter.getFps());
        GuiDraw.drawText(graphics, c, 5, lineY, scale, textColor, true);
        lineY -= shift;
        LocatedWidget locatedHovered = muiScreen.getPanelManager().getTopWidgetLocated(true);
        boolean showHovered = ModularUIConfig.Dev.showHovered();
        boolean showParent = ModularUIConfig.Dev.showParent();

        ITheme theme;
        if (locatedHovered != null && (showHovered || showParent)) {
            theme = locatedHovered.getElement().getPanel().getTheme();
        } else {
            theme = context.getTheme();
        }
        s = I18n.get("modularui.debug.theme_id", theme.getId());
        GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);

        if (locatedHovered != null && (showHovered || showParent)) {
            drawSegmentLine(graphics, lineY -= 4, scale, textColor);
            lineY -= 10;

            IWidget hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            graphics.pose().pushMatrix();
            context.applyTo(graphics.pose());

            Area area = hovered.getArea();
            IWidget parent = hovered.getParent();

            if (showHovered && ModularUIConfig.Dev.showOutline()) {
                GuiDraw.drawBorderOutsideXYWH(graphics, 0, 0, area.width, area.height, scale, outlineColor);
            }
            if (hovered.hasParent() && showParent && ModularUIConfig.Dev.showParentOutline()) {
                GuiDraw.drawBorderOutsideXYWH(graphics, -area.rx, -area.ry, parent.getArea().width,
                        parent.getArea().height, scale, Color.withAlpha(outlineColor, 0.3f));
            }
            graphics.pose().popMatrix();
            locatedHovered.unapplyMatrix(context);
            if (showHovered) {
                if (ModularUIConfig.Dev.showWidgetTheme()) {
                    s = I18n.get("modularui.debug.widget_theme", hovered.getWidgetTheme(hovered.getPanel().getTheme()).key().getFullName());
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                if (ModularUIConfig.Dev.showSize()) {
                    s = I18n.get("modularui.debug.size", area.width, area.height);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                if (ModularUIConfig.Dev.showPos()) {
                    s = I18n.get("modularui.debug.pos_rel", area.x, area.y, area.rx, area.ry);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                s = I18n.get("modularui.debug.widget", hovered);
                GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
            }
            if (hovered.hasParent() && showParent) {
                if (showHovered) {
                    drawSegmentLine(graphics, lineY -= 4, scale, textColor);
                    lineY -= 10;
                }
                if (ModularUIConfig.Dev.showParentWidgetTheme()) {
                    s = I18n.get("modularui.debug.widget_theme", parent.getWidgetTheme(parent.getPanel().getTheme()).key().getFullName());
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                area = parent.getArea();
                if (ModularUIConfig.Dev.showParentSize()) {
                    s = I18n.get("modularui.debug.parent_size", area.width, area.height);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                if (ModularUIConfig.Dev.showParentPos()) {
                    s = I18n.get("modularui.debug.parent_pos_rel", area.x, area.y, area.rx, area.ry);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                }
                s = I18n.get("modularui.debug.parent", parent);
                GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
            }
            if (ModularUIConfig.Dev.showExtra()) {
                if (hovered instanceof ItemSlot slotWidget) {
                    drawSegmentLine(graphics, lineY -= 4, scale, textColor);
                    lineY -= 10;
                    ModularSlot slot = slotWidget.getSlot();
                    s = I18n.get("modularui.debug.slot_index", slot.getContainerSlot());
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                    s = I18n.get("modularui.debug.slot_number", ((Slot) slot).index);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    lineY -= shift;
                    if (slotWidget.isSynced()) {
                        SlotGroup slotGroup = slot.getSlotGroup();
                        boolean allowShiftTransfer = slotGroup != null && slotGroup.isAllowShiftTransfer();
                        s = I18n.get("modularui.debug.shift_click_priority", allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED");
                        GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                    }
                } else if (hovered instanceof RichTextWidget richTextWidget) {
                    drawSegmentLine(graphics, lineY -= 4, scale, textColor);
                    lineY -= 10;
                    locatedHovered.applyMatrix(context);
                    Object hoveredElement = richTextWidget.getHoveredElement();
                    locatedHovered.unapplyMatrix(context);
                    if (hoveredElement instanceof FormattedCharSequence fcs) {
                        hoveredElement = FontRenderHelper.collectChars(fcs);
                    } else if (hoveredElement instanceof Component component) {
                        hoveredElement = component.getString();
                    }
                    s = I18n.get("modularui.debug.hovered", hoveredElement);
                    GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                } else if (hovered instanceof SchemaWidget sw) {
                    var r = sw.getSchemaRenderer();
                    var res = r.lastRayTrace();
                    if (r.captureDebugInfo() || res != null) {
                        drawSegmentLine(graphics, lineY -= 4, scale, textColor);
                        lineY -= 10;
                    }
                    if (r.captureDebugInfo()) {
                        var vec = sw.getSchemaRenderer().openGLMousePos();
                        s = I18n.get("modularui.debug.schema.debug", vec.z, vec.x, vec.y);
                        GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                        lineY -= shift;
                    }
                    if (res != null) {
                        String block = "Miss";
                        if (res.getType() == HitResult.Type.BLOCK) {
                            var bs = r.schema().getLevel().getBlockState(res.getBlockPos());
                            block = bs.getBlock().getName().getString();
                        }
                        s = I18n.get("modularui.debug.schema.raytrace", block,
                                res.getBlockPos().getX(), res.getBlockPos().getY(), res.getBlockPos().getZ());
                        GuiDraw.drawText(graphics, s, 5, lineY, scale, textColor, true);
                        lineY -= shift;
                    }
                }
            }
        }
        // dot at mouse pos
        GuiDraw.drawRect(graphics, mouseX, mouseY, 1, 1, ModularUIConfig.Dev.cursorColor());
    }

    private static void drawSegmentLine(GuiGraphicsExtractor graphics, int y, float scale, int color) {
        GuiDraw.drawRect(graphics, 5, y, 140 * scale, 1 * scale, color);
    }

    public static boolean hasScreen() {
        return currentScreen != null;
    }

    @Nullable
    public static Screen getMCScreen() {
        return MCHelper.getCurrentScreen();
    }

    @Nullable
    public static ModularScreen getMuiScreen() {
        return currentScreen;
    }

    @UnmodifiableView
    public static List<IMuiScreen> getMuiStack() {
        return Collections.unmodifiableList(muiStack);
    }

    private static boolean validateGui() {
        return validateGui(MCHelper.getCurrentScreen());
    }

    private static boolean validateGui(Screen screen) {
        if (currentScreen == null || !(screen instanceof IMuiScreen muiScreen)) {
            // no mui screen currently open
            return false;
        }
        if (screen != MCHelper.getCurrentScreen() || muiScreen.screen() != currentScreen) {
            defaultContext.reset();
            invalidateCurrentScreen();
            if (MCHelper.getCurrentScreen() == null) {
                invalidateMuiStack();
            }
            return false;
        }
        return true;
    }

    public static GuiContext getBestContext() {
        if (validateGui()) {
            return currentScreen.getContext();
        }
        return defaultContext;
    }

    private enum InputPhase {

        // for mui interactions
        EARLY,
        // for mc interactions (like E and ESC)
        LATE;

        public boolean isEarly() {
            return this == EARLY;
        }

        public boolean isLate() {
            return this == LATE;
        }
    }
}
