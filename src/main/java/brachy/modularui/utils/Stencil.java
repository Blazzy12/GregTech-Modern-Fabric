package brachy.modularui.utils;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A scissor stack for UIs. Before 26.x this drew stencil shapes so clipping followed 3D transforms; the 26.x GUI is
 * built from render states with no access to the stencil buffer, so this uses the GUI's own scissor stack, which
 * follows the 2D pose. Arbitrary stencil shapes fall back to their bounding rectangle.
 */
public class Stencil {

    // Stores a stack of areas that are transformed, so it represents the actual area
    private static final ObjectArrayList<Area> stencils = new ObjectArrayList<>();

    private final GuiContext context;

    @ApiStatus.Internal
    public Stencil(GuiContext context) {
        this.context = context;
    }

    /**
     * Resets the tracked areas. The GUI's scissor stack itself is reset by the game every frame.
     */
    public static void reset() {
        stencils.clear();
    }

    public void push(@NotNull Rectangle area) {
        push(area.x, area.y, area.width, area.height);
    }

    public void pushAtZero(@NotNull Rectangle area) {
        push(0, 0, area.width, area.height);
    }

    /**
     * Scissor a transformed part of the screen. The GUI pose applies to the scissor; viewport transformations of the
     * context are applied to the area kept in the stack.
     */
    public void push(float x, float y, float w, float h) {
        push(null, (int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(w), (int) Math.ceil(h));
    }

    /** Shapes cannot be stencilled any more; nothing is clipped. Kept for API compatibility. */
    public void push(Runnable stencilShape, boolean hideStencilShape) {
        push(stencilShape, 0, 0, 0, 0, hideStencilShape);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h) {
        push(stencilShape, x, y, w, h, true);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h, boolean hideStencilShape) {
        Area scissor = new Area(x, y, w, h);
        scissor.transformAndRectanglerize(this.context);
        if (!stencils.isEmpty()) {
            stencils.top().clamp(scissor);
        }
        this.context.getGraphics().enableScissor(x, y, x + w, y + h);
        stencils.add(scissor);
    }

    /**
     * Removes the top most scissor
     */
    public void pop() {
        if (stencils.isEmpty()) {
            throw new IllegalStateException("Tried to pop an empty stencil stack!");
        }
        stencils.pop();
        this.context.getGraphics().disableScissor();
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (stencils.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return stencils.top().intersects(Area.SHARED);
    }
}
