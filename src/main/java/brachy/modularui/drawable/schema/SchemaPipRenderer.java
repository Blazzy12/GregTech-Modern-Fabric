package brachy.modularui.drawable.schema;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.FilterMode;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Renders {@link BaseSchemaRenderer} scenes into their own render targets, so several schemas can be on screen at
 * once. Unlike vanilla picture-in-picture renderers this uses the schema's own camera and projection.
 */
public class SchemaPipRenderer extends PictureInPictureRenderer<SchemaRenderState> {

    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("PIP - schema");

    @Override
    public void prepare(SchemaRenderState state, GuiRenderState guiRenderState,
                        FeatureRenderDispatcher featureRenderDispatcher, int guiScale) {
        BaseSchemaRenderer renderer = state.renderer();
        Matrix3x2fc pose = state.pose();
        float scaleX = (float) Math.hypot(pose.m00(), pose.m01());
        float scaleY = (float) Math.hypot(pose.m10(), pose.m11());
        int width = Math.max(1, (int) Math.ceil((state.x1() - state.x0()) * scaleX * guiScale));
        int height = Math.max(1, (int) Math.ceil((state.y1() - state.y0()) * scaleY * guiScale));
        BaseSchemaRenderer.RenderTarget target = renderer.renderTarget(width, height);

        int clear = renderer.getClearColor();
        float a = (clear >>> 24) / 255f;
        // the blit expects premultiplied alpha
        Vector4f clearColor = new Vector4f(((clear >> 16) & 0xFF) / 255f * a, ((clear >> 8) & 0xFF) / 255f * a,
                (clear & 0xFF) / 255f * a, a);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(target.color(), clearColor, target.depth(), 0.0);

        RenderSystem.setProjectionMatrix(this.projectionBuffer.getBuffer(renderer.getProjection()),
                renderer.isIsometric() ? ProjectionType.ORTHOGRAPHIC : ProjectionType.PERSPECTIVE);
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.LEVEL);
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        renderer.submitScene(new PoseStack(), this.submitNodeStorage, state.partialTick());
        try (FeatureRenderDispatcher.PreparedFrame frame = featureRenderDispatcher.prepareFrame(this.submitNodeStorage);
             RenderPass renderPass = encoder.createRenderPass(() -> "Schema", target.colorView(), Optional.empty(),
                     target.depthView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(renderPass);
            FeatureRenderDispatcher.renderAllFeatures(renderPass, frame);
        }
        modelViewStack.popMatrix();

        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(target.colorView(),
                        RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                new Matrix3x2f(pose), state.x0(), state.y0(), state.x1(), state.y1(),
                0f, 1f, 1f, 0f, -1, state.scissorArea(), null));
    }

    @Override
    protected void renderToTexture(SchemaRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        // everything happens in prepare, with the schema's own projection
    }

    @Override
    public Class<SchemaRenderState> getRenderStateClass() {
        return SchemaRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "schema";
    }

    @Override
    public void close() {
        super.close();
        this.projectionBuffer.close();
    }
}
