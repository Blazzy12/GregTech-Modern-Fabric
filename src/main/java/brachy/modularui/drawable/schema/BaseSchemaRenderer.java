package brachy.modularui.drawable.schema;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.Icon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.SchemaWidget;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Renders a {@link ISchema} as a small 3D scene in a GUI.
 * <p>
 * Blocks and fluids are tesselated once on a background thread and cached; every frame the cached quads and the
 * block entities are submitted into the schema's own render target by {@link SchemaPipRenderer}, using this
 * renderer's camera.
 */
@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {

    private static final float NEAR = 0.05f;
    private static final float FAR = 10000.0f;
    private static final float FOV_Y = 60.0f * Mth.DEG_TO_RAD;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Vector3f UP = new Vector3f(0, 1, 0);

    @Getter private final ISchema schema;
    private final RenderLevel renderLevel;
    @Getter private final Camera camera = new Camera();
    @Getter private BlockHitResult lastRayTrace = null;
    @Getter private RenderFilter renderFilter = RenderFilter.ALL;

    private final SchemaMesh mesh;
    private boolean disposed;
    private @Nullable RenderTarget renderTarget;

    /** rotation part of the view, camera at the origin */
    private final Matrix4f viewRotation = new Matrix4f();
    @Getter private final Matrix4f projection = new Matrix4f();
    private float aspect = 1;
    @Getter @Setter private boolean captureDebugInfo;
    /** the mouse position in normalized device coordinates, for debugging */
    @Getter private final Vector3f openGLMousePos = new Vector3f();
    private final List<Vector3f> pos = new ArrayList<>();

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema, (pos, state) -> this.renderFilter.shouldRender(pos, state));
        this.mesh = new SchemaMesh(schema, this.renderLevel, () -> this.renderFilter, this::onRendered);
    }

    public void notifyRecompile() {
        this.mesh.markDirty();
    }

    public boolean isCompiling() {
        return this.mesh.isCompiling();
    }

    public boolean isCompleted() {
        return this.mesh.isCompiled();
    }

    public void dispose() {
        this.disposed = true;
        this.mesh.dispose();
        if (this.renderTarget != null) {
            this.renderTarget.close();
            this.renderTarget = null;
        }
    }

    @Override
    public SchemaWidget asWidget() {
        return new SchemaWidget(this);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(50);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (width <= 0 || height <= 0 || this.disposed) return;
        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();

        onSetupCamera();
        setupCamera(width, height);
        this.mesh.update();

        if (doRayTrace() || captureDebugInfo()) {
            BlockHitResult result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX - x, mouseY - y, width, height);
            }
            if (result == null || result.getType() != HitResult.Type.BLOCK) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(result);
            }
            this.lastRayTrace = result;
        }

        GuiGraphicsExtractor graphics = context.getGraphics();
        graphics.guiRenderState.addPicturesInPictureState(new SchemaRenderState(this,
                new org.joml.Matrix3x2f(graphics.pose()), x, y, x + width, y + height,
                context.getRenderPartialTicks(), graphics.scissorStack.peek()));

        if (this.captureDebugInfo) {
            drawProjectedBlockPos(graphics, x, y, width, height);
        }
    }

    public void drawProjectedBlockPos(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        for (Vector3f s : this.pos) {
            GuiDraw.drawRect(graphics, x + s.x - 1, y + s.y - 1, 2, 2, Color.withAlpha(Color.BLUE.main, 1f));
        }
    }

    /** @return the pose that transforms world positions into view space */
    public PoseStack createWorldRenderPose() {
        PoseStack poseStack = new PoseStack();
        applyView(poseStack);
        return poseStack;
    }

    private void applyView(PoseStack poseStack) {
        poseStack.mulPose(this.viewRotation);
        Vector3f eye = this.camera.pos();
        poseStack.translate(-eye.x, -eye.y, -eye.z);
    }

    /**
     * Submits the whole scene. Called by {@link SchemaPipRenderer} while rendering into this schema's target.
     */
    @ApiStatus.Internal
    public void submitScene(PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        poseStack.pushPose();
        applyView(poseStack);
        this.mesh.submit(poseStack, collector, partialTick, this.camera.pos(), isBEREnabled());
        if (this.captureDebugInfo) {
            for (var e : this.schema) {
                if (!e.getValue().isAir()) {
                    BlockPos p = e.getKey();
                    poseStack.pushPose();
                    poseStack.translate(p.getX(), p.getY(), p.getZ());
                    collector.submitShapeOutline(poseStack, Shapes.block(), RenderTypes.lines(), 0xFFFF0000, 1f,
                            false);
                    poseStack.popPose();
                }
            }
        }
        submitExtras(poseStack, collector);
        poseStack.popPose();
    }

    /** @return the render target of this schema, (re)created at the given size */
    @ApiStatus.Internal
    public RenderTarget renderTarget(int width, int height) {
        if (this.renderTarget == null || this.renderTarget.width != width || this.renderTarget.height != height) {
            if (this.renderTarget != null) this.renderTarget.close();
            this.renderTarget = new RenderTarget(width, height);
        }
        return this.renderTarget;
    }

    @ApiStatus.Internal
    public Matrix4f getProjection() {
        return this.projection;
    }

    protected void setupCamera(int width, int height) {
        this.aspect = (float) width / height;
        Vector3f dir = this.camera.getLookVec();
        if (dir.lengthSquared() < 1.0e-8f) dir.set(0, 0, -1);
        this.viewRotation.identity().lookAlong(dir, UP);

        boolean zZeroToOne = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
        // near and far are swapped like vanilla does, for reversed depth
        if (isIsometric()) {
            float halfHeight = Math.max(this.camera.dist(), 1f) * (float) Math.tan(FOV_Y * 0.5);
            float halfWidth = halfHeight * this.aspect;
            this.projection.setOrtho(-halfWidth, halfWidth, -halfHeight, halfHeight, FAR, -FAR, zZeroToOne);
        } else {
            this.projection.setPerspective(FOV_Y, this.aspect, FAR, NEAR, zZeroToOne);
        }
    }

    /**
     * Traces a ray from the given mouse pos into the rendered scene.
     *
     * @param mouseX A mouse x pos from 0 to width
     * @param mouseY A mouse y pos from 0 to height
     * @param width  Width of the drawn area
     * @param height Height of the drawn area
     * @return raytrace result
     */
    protected BlockHitResult rayTrace(int mouseX, int mouseY, int width, int height) {
        if (this.captureDebugInfo) {
            Matrix4f viewProjection = new Matrix4f(this.projection).mul(createWorldRenderPose().last().pose());
            int i = 0;
            Vector4f clip = new Vector4f();
            for (var e : this.schema) {
                if (!e.getValue().isAir()) {
                    var p = e.getKey();
                    viewProjection.transform(p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f, 1f, clip);
                    Vector3f vec = this.pos.size() > i ? this.pos.get(i) : new Vector3f();
                    vec.set((clip.x / clip.w + 1) * 0.5f * width, (1 - clip.y / clip.w) * 0.5f * height, clip.w);
                    if (this.pos.size() > i) this.pos.set(i, vec);
                    else this.pos.add(vec);
                    i++;
                }
            }
            while (i < this.pos.size()) this.pos.remove(i);
        }
        float ndcX = 2f * mouseX / width - 1f;
        float ndcY = 1f - 2f * mouseY / height;
        this.openGLMousePos.set(ndcX, ndcY, 0);

        Matrix4f inverseView = new Matrix4f(this.viewRotation).invert();
        Vector3f eye = this.camera.pos();
        float tanHalf = (float) Math.tan(FOV_Y * 0.5);
        Vector3f start, direction;
        if (isIsometric()) {
            float halfHeight = Math.max(this.camera.dist(), 1f) * tanHalf;
            Vector3f offset = inverseView.transformDirection(ndcX * halfHeight * this.aspect, ndcY * halfHeight, 0,
                    new Vector3f());
            direction = inverseView.transformDirection(0, 0, -1, new Vector3f()).normalize();
            start = new Vector3f(eye).add(offset).sub(new Vector3f(direction).mul(1000f));
        } else {
            direction = inverseView.transformDirection(ndcX * tanHalf * this.aspect, ndcY * tanHalf, -1,
                    new Vector3f()).normalize();
            start = new Vector3f(eye);
        }
        float reach = 2000f + this.camera.dist() * 4;
        Vector3f end = new Vector3f(direction).mul(reach).add(start);

        ClipContext context = new ClipContext(new Vec3(start), new Vec3(end), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY, CollisionContext.empty());
        return this.renderLevel.clip(context);
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(BlockHitResult result) {}

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {}

    /** Submits additional geometry in world space, like highlights. */
    @ApiStatus.OverrideOnly
    protected void submitExtras(PoseStack poseStack, SubmitNodeCollector collector) {}

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(Color.WHITE.main, 0.5f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isBEREnabled() {
        return true;
    }

    /**
     * Sets a render filter for the block to be rendered. This also causes the world to rerender.
     * This method or {@link #notifyRecompile()} needs to be called everytime the render filter changes.
     */
    public void updateRenderFilter(RenderFilter renderFilter) {
        notifyRecompile();
        this.renderFilter = renderFilter != null ? renderFilter : RenderFilter.ALL;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseSchemaRenderer that)) return false;

        return this.schema.equals(that.schema) && this.renderLevel.equals(that.renderLevel) &&
                this.camera.equals(that.camera) && Objects.equals(this.renderFilter, that.renderFilter);
    }

    @Override
    public int hashCode() {
        int result = this.schema.hashCode();
        result = 31 * result + this.renderLevel.hashCode();
        result = 31 * result + this.camera.hashCode();
        result = 31 * result + Objects.hashCode(this.renderFilter);
        return result;
    }

    /** The colour and depth textures one schema renders into. */
    @ApiStatus.Internal
    public static final class RenderTarget implements AutoCloseable {

        private final int width;
        private final int height;
        private final GpuTexture color;
        private final GpuTextureView colorView;
        private final GpuTexture depth;
        private final GpuTextureView depthView;

        private RenderTarget(int width, int height) {
            this.width = width;
            this.height = height;
            GpuDevice device = RenderSystem.getDevice();
            // usages copied from vanilla's picture-in-picture textures
            this.color = device.createTexture(() -> "UI schema texture", 13, GpuFormat.RGBA8_UNORM, width, height,
                    1, 1);
            this.colorView = device.createTextureView(this.color);
            this.depth = device.createTexture(() -> "UI schema depth texture", 9, GpuFormat.D32_FLOAT, width,
                    height, 1, 1);
            this.depthView = device.createTextureView(this.depth);
        }

        public GpuTexture color() {
            return color;
        }

        public GpuTextureView colorView() {
            return colorView;
        }

        public GpuTexture depth() {
            return depth;
        }

        public GpuTextureView depthView() {
            return depthView;
        }

        @Override
        public void close() {
            this.colorView.close();
            this.color.close();
            this.depthView.close();
            this.depth.close();
        }
    }
}
