package brachy.modularui.utils;

import brachy.modularui.api.widget.ResizeDragArea;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Cursor shapes for UIs. 26.x has a vanilla cursor API (and SDL instead of GLFW), so the icon is requested from the
 * GUI each frame by {@link #apply(GuiGraphicsExtractor)}. Diagonal resizing falls back to the omnidirectional cursor,
 * as vanilla has no diagonal shapes.
 */
public class CursorHandler {

    public enum CursorIcon {

        DEFAULT,
        TEXT,
        POINTER,
        NOT_ALLOWED,
        CROSSHAIR,
        RESIZE_HORIZONTAL,
        RESIZE_VERTICAL,
        RESIZE_TL_BR,
        RESIZE_TR_BL,
        RESIZE_ALL,
    }

    private static CursorIcon current = CursorIcon.DEFAULT;

    public static void setCursorResizeIcon(@Nullable ResizeDragArea dragArea) {
        if (dragArea == null) {
            resetCursorIcon();
            return;
        }
        CursorIcon icon = switch (dragArea) {
            case TOP_LEFT, BOTTOM_RIGHT -> CursorIcon.RESIZE_TL_BR;
            case TOP_RIGHT, BOTTOM_LEFT -> CursorIcon.RESIZE_TR_BL;
            case TOP, BOTTOM -> CursorIcon.RESIZE_VERTICAL;
            case RIGHT, LEFT -> CursorIcon.RESIZE_HORIZONTAL;
        };
        setCursorIcon(icon);
    }

    public static void setCursorIcon(CursorIcon cursorIcon) {
        current = cursorIcon;
    }

    public static void resetCursorIcon() {
        setCursorIcon(CursorIcon.DEFAULT);
    }

    /** Requests the current icon for this frame; called while a MUI screen draws. */
    @ApiStatus.Internal
    public static void apply(GuiGraphicsExtractor graphics) {
        if (current == CursorIcon.DEFAULT) return;
        graphics.requestCursor(toVanilla(current));
    }

    private static CursorType toVanilla(CursorIcon icon) {
        return switch (icon) {
            case DEFAULT -> CursorTypes.ARROW;
            case TEXT -> CursorTypes.IBEAM;
            case POINTER -> CursorTypes.POINTING_HAND;
            case CROSSHAIR -> CursorTypes.CROSSHAIR;
            case RESIZE_HORIZONTAL -> CursorTypes.RESIZE_EW;
            case RESIZE_VERTICAL -> CursorTypes.RESIZE_NS;
            case RESIZE_TL_BR, RESIZE_TR_BL, RESIZE_ALL -> CursorTypes.RESIZE_ALL;
            case NOT_ALLOWED -> CursorTypes.NOT_ALLOWED;
        };
    }

    @ApiStatus.Internal
    public static void init() {}
}
