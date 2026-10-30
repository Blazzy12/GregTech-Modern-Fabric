package brachy.modularui.drawable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

/**
 * Free-form GUI geometry for the 26.x render-state GUI, which no longer hands out vertex buffers. Vertices are
 * recorded in GUI space and submitted as one {@link GuiElementRenderState}; triangle fans and strips are turned into
 * plain triangles, since GUI batching only understands lists.
 * <p>
 * Usage: {@code MuiGeometry.quads(graphics, texture).vertex(x, y).uv(u, v).color(c)...submit()}.
 */
public final class MuiGeometry {

    /** Plain triangles, as vanilla only registers quad GUI pipelines. */
    public static final RenderPipeline GUI_TRIANGLES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("modularui", "pipeline/gui_triangles"))
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build());
    public static final RenderPipeline GUI_TEXTURED_TRIANGLES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("modularui", "pipeline/gui_textured_triangles"))
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build());

    private enum Mode {
        QUADS,
        TRIANGLES,
        FAN,
        STRIP
    }

    private final GuiGraphicsExtractor graphics;
    private final Mode mode;
    private final @Nullable TextureSetup texture;
    private final FloatArrayList xy = new FloatArrayList();
    private final FloatArrayList uv = new FloatArrayList();
    private final IntArrayList colors = new IntArrayList();
    private int tint = -1;

    private MuiGeometry(GuiGraphicsExtractor graphics, Mode mode, @Nullable TextureSetup texture) {
        this.graphics = graphics;
        this.mode = mode;
        this.texture = texture;
        // like the old global shader colour (used for panel fade alpha)
        this.tint = GuiDraw.currentColor();
    }

    public static MuiGeometry quads(GuiGraphicsExtractor graphics) {
        return new MuiGeometry(graphics, Mode.QUADS, null);
    }

    public static MuiGeometry quads(GuiGraphicsExtractor graphics, TextureSetup texture) {
        return new MuiGeometry(graphics, Mode.QUADS, texture);
    }

    public static MuiGeometry triangles(GuiGraphicsExtractor graphics) {
        return new MuiGeometry(graphics, Mode.TRIANGLES, null);
    }

    public static MuiGeometry triangles(GuiGraphicsExtractor graphics, TextureSetup texture) {
        return new MuiGeometry(graphics, Mode.TRIANGLES, texture);
    }

    public static MuiGeometry fan(GuiGraphicsExtractor graphics) {
        return new MuiGeometry(graphics, Mode.FAN, null);
    }

    public static MuiGeometry strip(GuiGraphicsExtractor graphics) {
        return new MuiGeometry(graphics, Mode.STRIP, null);
    }

    public static MuiGeometry fan(GuiGraphicsExtractor graphics, TextureSetup texture) {
        return new MuiGeometry(graphics, Mode.FAN, texture);
    }

    public static MuiGeometry strip(GuiGraphicsExtractor graphics, TextureSetup texture) {
        return new MuiGeometry(graphics, Mode.STRIP, texture);
    }

    /** The texture setup of a texture file or atlas. */
    public static TextureSetup texture(Identifier location) {
        var texture = net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(location);
        return TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
    }

    /** Multiplies every following vertex color, like the old shader color. */
    public MuiGeometry tint(int argb) {
        this.tint = argb;
        return this;
    }

    public MuiGeometry vertex(float x, float y) {
        xy.add(x);
        xy.add(y);
        uv.add(0);
        uv.add(0);
        colors.add(tint);
        return this;
    }

    public MuiGeometry uv(float u, float v) {
        int i = uv.size() - 2;
        uv.set(i, u);
        uv.set(i + 1, v);
        return this;
    }

    public MuiGeometry color(int argb) {
        colors.set(colors.size() - 1, multiply(argb, tint));
        return this;
    }

    public MuiGeometry color(float r, float g, float b, float a) {
        return color(((int) (a * 255) & 0xFF) << 24 | ((int) (r * 255) & 0xFF) << 16 |
                ((int) (g * 255) & 0xFF) << 8 | ((int) (b * 255) & 0xFF));
    }

    public MuiGeometry color(int r, int g, int b, int a) {
        return color((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
    }

    public int vertexCount() {
        return colors.size();
    }

    /** Adds the recorded geometry to the GUI at the current pose and scissor. */
    public void submit() {
        int n = vertexCount();
        if (n == 0) return;
        int[] order = switch (mode) {
            case QUADS, TRIANGLES -> null;
            case FAN -> fanOrder(n);
            case STRIP -> stripOrder(n);
        };
        boolean quads = mode == Mode.QUADS;
        RenderPipeline pipeline = texture == null ?
                (quads ? RenderPipelines.GUI : GUI_TRIANGLES) :
                (quads ? RenderPipelines.GUI_TEXTURED : GUI_TEXTURED_TRIANGLES);
        int count = order == null ? n - n % (quads ? 4 : 3) : order.length;
        if (count <= 0) return;
        float[] xs = new float[count * 2];
        float[] us = new float[count * 2];
        int[] cs = new int[count];
        for (int i = 0; i < count; i++) {
            int src = order == null ? i : order[i];
            xs[i * 2] = xy.getFloat(src * 2);
            xs[i * 2 + 1] = xy.getFloat(src * 2 + 1);
            us[i * 2] = uv.getFloat(src * 2);
            us[i * 2 + 1] = uv.getFloat(src * 2 + 1);
            cs[i] = colors.getInt(src);
        }
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.scissorStack.peek();
        graphics.guiRenderState.addGuiElement(new State(pipeline,
                texture == null ? TextureSetup.noTexture() : texture, pose, xs, us, cs, texture != null, scissor,
                bounds(xs, pose, scissor)));
    }

    private static int[] fanOrder(int n) {
        if (n < 3) return new int[0];
        int[] order = new int[(n - 2) * 3];
        for (int i = 1, j = 0; i < n - 1; i++) {
            order[j++] = 0;
            order[j++] = i;
            order[j++] = i + 1;
        }
        return order;
    }

    private static int[] stripOrder(int n) {
        if (n < 3) return new int[0];
        int[] order = new int[(n - 2) * 3];
        for (int i = 0, j = 0; i < n - 2; i++) {
            // keep a consistent winding, as GL strips do
            if ((i & 1) == 0) {
                order[j++] = i;
                order[j++] = i + 1;
            } else {
                order[j++] = i + 1;
                order[j++] = i;
            }
            order[j++] = i + 2;
        }
        return order;
    }

    private static int multiply(int a, int b) {
        if (b == -1) return a;
        int alpha = ((a >>> 24) * (b >>> 24)) / 255;
        int red = (((a >> 16) & 0xFF) * ((b >> 16) & 0xFF)) / 255;
        int green = (((a >> 8) & 0xFF) * ((b >> 8) & 0xFF)) / 255;
        int blue = ((a & 0xFF) * (b & 0xFF)) / 255;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static @Nullable ScreenRectangle bounds(float[] xs, Matrix3x2fc pose, @Nullable ScreenRectangle scissor) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < xs.length; i += 2) {
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, xs[i + 1]);
            maxY = Math.max(maxY, xs[i + 1]);
        }
        int x0 = (int) Math.floor(minX), y0 = (int) Math.floor(minY);
        ScreenRectangle rect = new ScreenRectangle(x0, y0, Math.max(1, (int) Math.ceil(maxX) - x0),
                Math.max(1, (int) Math.ceil(maxY) - y0)).transformMaxBounds(pose);
        return scissor != null ? scissor.intersection(rect) : rect;
    }

    private record State(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] us,
                         int[] colors, boolean textured, @Nullable ScreenRectangle scissorArea,
                         @Nullable ScreenRectangle bounds)
            implements GuiElementRenderState {

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (int i = 0; i < colors.length; i++) {
                VertexConsumer v = consumer.addVertexWith2DPose(pose, xs[i * 2], xs[i * 2 + 1]);
                if (textured) v.setUv(us[i * 2], us[i * 2 + 1]);
                v.setColor(colors[i]);
            }
        }
    }
}
