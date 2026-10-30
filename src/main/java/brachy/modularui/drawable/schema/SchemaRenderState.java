package brachy.modularui.drawable.schema;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fc;

/**
 * GUI render state of one {@link BaseSchemaRenderer} draw, rendered by {@link SchemaPipRenderer}.
 */
public record SchemaRenderState(BaseSchemaRenderer renderer, Matrix3x2fc pose, int x0, int y0, int x1, int y1,
                                float partialTick, @Nullable ScreenRectangle scissorArea,
                                @Nullable ScreenRectangle bounds)
        implements PictureInPictureRenderState {

    public SchemaRenderState(BaseSchemaRenderer renderer, Matrix3x2fc pose, int x0, int y0, int x1, int y1,
                             float partialTick, @Nullable ScreenRectangle scissorArea) {
        this(renderer, pose, x0, y0, x1, y1, partialTick, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public float scale() {
        return 1f;
    }
}
