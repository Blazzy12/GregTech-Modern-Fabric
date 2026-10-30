package brachy.modularui.drawable.progress;

import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import net.minecraft.util.Mth;

import lombok.Getter;

/**
 * A progress texture which translates the progress into a circular angle. This works with any {@link brachy.modularui.api.drawable.IDrawable}.
 */
public class CircularProgressDrawable extends AbstractProgressDrawable<CircularProgressDrawable> {

    @Getter private Direction direction = Direction.CW;

    @Override
    protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height,
                                       WidgetTheme widgetTheme) {
        context.getStencil().push(x, y, width, height);
    }

    /**
     * Only rectangular clipping exists in the 26.x GUI, so the swept sector is drawn as runs of one pixel wide
     * columns, each clipped to the part of the column inside the sector.
     */
    @Override
    protected void drawPartial(float progress, GuiContext context, int x, int y, int width, int height,
                               WidgetTheme widgetTheme) {
        float angle = progress * Mth.TWO_PI;
        float xc = x + width / 2f, yc = y + height / 2f;
        for (int col = 0; col < width; col++) {
            float dx = x + col + 0.5f - xc;
            int runStart = -1;
            for (int row = 0; row <= height; row++) {
                boolean inside = row < height && isInside(dx, y + row + 0.5f - yc, angle);
                if (inside && runStart < 0) {
                    runStart = row;
                } else if (!inside && runStart >= 0) {
                    context.getStencil().push(x + col, y + runStart, 1, row - runStart);
                    getFilledTexture().draw(context, x, y, width, height, widgetTheme);
                    context.getStencil().pop();
                    runStart = -1;
                }
            }
        }
    }

    private boolean isInside(float dx, float dy, float angle) {
        // angle measured from the top centre, in the drawing direction
        float a = (float) Math.atan2(this.direction == Direction.CW ? dx : -dx, -dy);
        if (a < 0) a += Mth.TWO_PI;
        return a <= angle;
    }

    public CircularProgressDrawable direction(Direction direction) {
        this.direction = direction == null ? Direction.CW : direction;
        return this;
    }

    public CircularProgressDrawable clockwise() {
        return direction(Direction.CW);
    }

    public CircularProgressDrawable counterClockwise() {
        return direction(Direction.CCW);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircularProgressDrawable that)) return false;
        if (!super.equals(o)) return false;

        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public enum Direction {
        CW, CCW
    }
}
