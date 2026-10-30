package brachy.modularui.utils;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * ModularUI numbers mouse buttons like GLFW did (0 left, 1 right, 2 middle). Minecraft 26.x numbers them like SDL
 * (1 left, 2 middle, 3 right), so events are converted at the screen boundary.
 */
public final class MouseButtons {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int MIDDLE = 2;

    private MouseButtons() {}

    public static int toLegacy(int button) {
        return switch (button) {
            case InputConstants.MOUSE_BUTTON_LEFT -> LEFT;
            case InputConstants.MOUSE_BUTTON_RIGHT -> RIGHT;
            case InputConstants.MOUSE_BUTTON_MIDDLE -> MIDDLE;
            default -> button - 1;
        };
    }

    public static int fromLegacy(int button) {
        return switch (button) {
            case LEFT -> InputConstants.MOUSE_BUTTON_LEFT;
            case RIGHT -> InputConstants.MOUSE_BUTTON_RIGHT;
            case MIDDLE -> InputConstants.MOUSE_BUTTON_MIDDLE;
            default -> button + 1;
        };
    }
}
