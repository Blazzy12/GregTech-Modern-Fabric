package brachy.modularui.drawable.schema;

import brachy.modularui.utils.FluidTextureType;
import brachy.modularui.integration.embeddium.SodiumCompat;
import brachy.modularui.ModularUI;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
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
import net.minecraft.util.Util;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The blocks, fluids and block entities of a schema, tesselated on a background thread and submitted each frame.
 * Everything is lit full-bright.
 */
public class SchemaMesh {

    private static final int FULL_BRIGHT = 0xF000F0;

    private final ISchema schema;
    private final BlockAndTintGetter level;
    private final Supplier<RenderFilter> renderFilter;
    private final Runnable onCompiled;

    private @Nullable CompileTask compileTask;
    private volatile @Nullable CompileResults compiled;
    private boolean dirty = true;
    private boolean disposed;

    /**
     * @param level      the view of the schema's level to tesselate with
     * @param onCompiled called on the render thread when a new mesh is ready
     */
    public SchemaMesh(ISchema schema, BlockAndTintGetter level, Supplier<RenderFilter> renderFilter,
                      Runnable onCompiled) {
        this.schema = schema;
        this.level = level;
        this.renderFilter = renderFilter;
        this.onCompiled = onCompiled;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isCompiling() {
        return this.compileTask != null && !this.compileTask.done;
    }

    public boolean isCompiled() {
        return this.compiled != null;
    }

    public void dispose() {
        this.disposed = true;
        if (this.compileTask != null) {
            this.compileTask.cancel();
            this.compileTask = null;
        }
        this.compiled = null;
    }

    /** Starts recompiling if the mesh is dirty. Call every frame. */
    public void update() {
        if (this.disposed) return;
        if (this.dirty && !isCompiling()) {
            this.dirty = false;
            if (this.compileTask != null) this.compileTask.cancel();
            CompileTask task = new CompileTask();
            this.compileTask = task;
            CompletableFuture.supplyAsync(task::compile, Util.backgroundExecutor())
                    .whenComplete((result, error) -> {
                        task.done = true;
                        if (error != null) {
                            Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Compiling schema"));
                        } else if (result != null && !task.isCanceled.get()) {
                            Minecraft.getInstance().execute(() -> {
                                if (this.disposed || task.isCanceled.get()) return;
                                this.compiled = result;
                                this.onCompiled.run();
                            });
                        }
                    });
        }
    }

    /**
     * Submits the mesh, in the space of the pose stack's current pose.
     *
     * @param eye the camera position in mesh space, for sorting translucent quads
     */
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, float partialTick, Vector3fc eye,
                       boolean blockEntities) {
        CompileResults results = this.compiled;
        if (results == null) return;
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            List<CachedQuad> quads = results.quads.get(layer);
            if (quads != null && !quads.isEmpty()) {
                if (layer.translucent()) {
                    quads.sort(Comparator.comparingDouble((CachedQuad q) -> -q.distanceSq(eye)));
                }
                collector.submitCustomGeometry(poseStack, renderType(layer), (pose, buffer) -> {
                    PoseStack.Pose quadPose = pose.copy();
                    for (CachedQuad quad : quads) {
                        quadPose.set(pose);
                        quadPose.translate(quad.x, quad.y, quad.z);
                        buffer.putBakedQuad(quadPose, quad.quad, quad.instance);
                    }
                });
            }
            RecordedVertices fluid = results.fluids.get(layer);
            if (fluid != null && !fluid.isEmpty()) {
                collector.submitCustomGeometry(poseStack, renderType(layer), fluid::replay);
            }
        }
        // Sodium only animates sprites it saw in the world
        if (!results.fluidSprites.isEmpty() && ModularUI.Mods.SODIUM.isLoaded()) {
            SodiumCompat.markSpritesAsActive(results.fluidSprites);
        }
        if (blockEntities) {
            CameraRenderState cameraState = new CameraRenderState();
            cameraState.pos = new Vec3(eye.x(), eye.y(), eye.z());
            cameraState.blockPos = BlockPos.containing(cameraState.pos);
            cameraState.initialized = true;
            for (BlockEntity blockEntity : results.blockEntities) {
                submitBlockEntity(blockEntity, poseStack, collector, cameraState, partialTick);
            }
        }
    }

    private static RenderType renderType(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    private static <E extends BlockEntity, S extends BlockEntityRenderState> void submitBlockEntity(
            E blockEntity, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState,
            float partialTick) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BlockEntityRenderer<E, S> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null || !blockEntity.hasLevel()) return;
        S state = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, state, partialTick, cameraState.pos, null);
        state.lightCoords = FULL_BRIGHT;
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        dispatcher.submit(state, poseStack, collector, cameraState);
        poseStack.popPose();
    }

    private record CachedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {

        double distanceSq(Vector3fc eye) {
            double dx = x + 0.5 - eye.x(), dy = y + 0.5 - eye.y(), dz = z + 0.5 - eye.z();
            return dx * dx + dy * dy + dz * dz;
        }
    }

    protected static final class CompileResults {

        private final Map<ChunkSectionLayer, List<CachedQuad>> quads = new EnumMap<>(ChunkSectionLayer.class);
        private final Map<ChunkSectionLayer, RecordedVertices> fluids = new EnumMap<>(ChunkSectionLayer.class);
        private final java.util.Set<net.minecraft.client.renderer.texture.TextureAtlasSprite> fluidSprites =
                new java.util.HashSet<>();
        private final List<BlockEntity> blockEntities = new ArrayList<>();
    }

    private final class CompileTask {

        private final AtomicBoolean isCanceled = new AtomicBoolean(false);
        private volatile boolean done;

        void cancel() {
            this.isCanceled.set(true);
        }

        @Nullable
        CompileResults compile() {
            if (this.isCanceled.get()) return null;
            Minecraft mc = Minecraft.getInstance();
            BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();
            ModelBlockRenderer blockRenderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), true,
                    mc.getBlockColors());
            FluidRenderer fluidRenderer = new FluidRenderer(mc.getModelManager().getFluidStateModelSet());
            CompileResults results = new CompileResults();
            BlockQuadOutput quadOutput = (x, y, z, quad, instance) -> results.quads
                    .computeIfAbsent(quad.materialInfo().layer(), l -> new ArrayList<>())
                    .add(new CachedQuad(x, y, z, quad, fullBrightCopy(instance)));
            BlockPos.MutableBlockPos fluidOrigin = new BlockPos.MutableBlockPos();
            FluidRenderer.Output fluidOutput = layer -> results.fluids
                    .computeIfAbsent(layer, l -> new RecordedVertices())
                    .at(fluidOrigin);

            BlockModelLighter.enableCaching();
            try {
                for (var blockEntry : schema) {
                    if (this.isCanceled.get()) return null;
                    BlockPos pos = blockEntry.getKey();
                    BlockState blockState = blockEntry.getValue();
                    if (!renderFilter.get().shouldRender(pos, blockState) || blockState.isAir()) continue;

                    if (blockState.hasBlockEntity()) {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity != null) results.blockEntities.add(blockEntity);
                    }

                    FluidState fluidState = blockState.getFluidState();
                    if (!fluidState.isEmpty()) {
                        // the fluid renderer writes section relative positions
                        fluidOrigin.set(SectionPos.blockToSectionCoord(pos.getX()) << 4,
                                SectionPos.blockToSectionCoord(pos.getY()) << 4,
                                SectionPos.blockToSectionCoord(pos.getZ()) << 4);
                        fluidRenderer.tesselate(level, pos, fluidOutput, blockState, fluidState);
                        var extensions = com.gregtechceu.gtceu.compat.client.extensions.common
                                .IClientFluidTypeExtensions.of(fluidState);
                        results.fluidSprites.add(FluidTextureType.STILL.map(extensions));
                        results.fluidSprites.add(FluidTextureType.FLOWING.map(extensions));
                    }

                    if (blockState.getRenderShape() == RenderShape.MODEL) {
                        blockRenderer.tesselateBlock(quadOutput, pos.getX(), pos.getY(), pos.getZ(), level,
                                pos, blockState, models.get(blockState), blockState.getSeed(pos));
                    }
                }
            } finally {
                BlockModelLighter.clearCache();
            }
            return results;
        }
    }

    private static QuadInstance fullBrightCopy(QuadInstance instance) {
        QuadInstance copy = new QuadInstance();
        for (int i = 0; i < 4; i++) {
            copy.setColor(i, instance.getColor(i));
        }
        copy.setLightCoords(FULL_BRIGHT);
        copy.setOverlayCoords(instance.overlayCoords());
        return copy;
    }

    /** Vertices written by the fluid renderer, replayed with a pose each frame. */
    private static final class RecordedVertices implements VertexConsumer {

        private final FloatArrayList floats = new FloatArrayList();
        private final IntArrayList ints = new IntArrayList();
        private float ox, oy, oz;

        RecordedVertices at(BlockPos origin) {
            this.ox = origin.getX();
            this.oy = origin.getY();
            this.oz = origin.getZ();
            return this;
        }

        boolean isEmpty() {
            return ints.isEmpty();
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords,
                              int lightCoords, float nx, float ny, float nz) {
            floats.add(x + ox);
            floats.add(y + oy);
            floats.add(z + oz);
            floats.add(u);
            floats.add(v);
            floats.add(nx);
            floats.add(ny);
            floats.add(nz);
            ints.add(color);
            ints.add(overlayCoords);
        }

        void replay(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f p = new Vector3f();
            Vector3f n = new Vector3f();
            for (int i = 0, f = 0; i < ints.size(); i += 2, f += 8) {
                pose.pose().transformPosition(floats.getFloat(f), floats.getFloat(f + 1), floats.getFloat(f + 2), p);
                pose.transformNormal(floats.getFloat(f + 5), floats.getFloat(f + 6), floats.getFloat(f + 7), n);
                buffer.addVertex(p.x, p.y, p.z, ints.getInt(i), floats.getFloat(f + 3), floats.getFloat(f + 4),
                        ints.getInt(i + 1), FULL_BRIGHT, n.x, n.y, n.z);
            }
        }

        // the fluid renderer only uses the bulk vertex method above
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setColor(int color) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            throw new UnsupportedOperationException();
        }
    }
}
