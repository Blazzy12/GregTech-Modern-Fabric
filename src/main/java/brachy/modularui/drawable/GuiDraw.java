package brachy.modularui.drawable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.compat.client.gui.TextColors;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.event.RichTooltipEvent;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.RectangleF;
import brachy.modularui.utils.math.NumberFormat;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import com.gregtechceu.gtceu.compat.client.event.RenderTooltipEvent;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;
import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiConsumer;

import static brachy.modularui.drawable.UITexture.GUI_TEXTURE_ID_CONVERTER;
import static net.minecraft.util.Mth.HALF_PI;
import static net.minecraft.util.Mth.TWO_PI;

/**
 * Drawing helpers for ModularUI on the 26.x render-state GUI. Geometry goes through {@link MuiGeometry}; the old GL
 * state ({@code RenderSystem.setShaderTexture}, shader colour) is emulated with {@link #bindTexture} and
 * {@link #setColor}. Z values are accepted for compatibility but ignored: GUI layering follows submission order.
 */
public class GuiDraw {

    private static final TextRenderer textRenderer = new TextRenderer();

    /** The texture that texture draws without a location use, like the old bound shader texture. */
    private static TextureSetup currentTexture = TextureSetup.noTexture();
    /** Multiplied into textured draws, like the old shader colour. */
    private static int shaderColor = -1;

    // ---------- state ----------

    public static void bindTexture(Identifier location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        currentTexture = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
    }

    public static void bindTexture(TextureAtlasSprite sprite) {
        bindTexture(sprite.atlasLocation());
    }

    public static TextureSetup currentTexture() {
        return currentTexture;
    }

    public static void setColor(float r, float g, float b, float a) {
        shaderColor = Color.argb((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public static void setColor(int argb) {
        shaderColor = argb;
    }

    public static void resetColor() {
        shaderColor = -1;
    }

    /** The current emulated shader colour, applied to all MUI geometry. */
    public static int currentColor() {
        return shaderColor;
    }

    private static MuiGeometry texturedQuads(GuiGraphicsExtractor graphics) {
        return MuiGeometry.quads(graphics, currentTexture);
    }

    // ---------- rectangles ----------

    public static void drawRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h, int color) {
        MuiGeometry geo = MuiGeometry.quads(graphics);
        drawRectRaw(geo, x0, y0, x0 + w, y0 + h, color);
        geo.submit();
    }

    public static void drawHorizontalGradientRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                  int colorLeft, int colorRight) {
        drawRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawVerticalGradientRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                int colorTop, int colorBottom) {
        drawRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom);
    }

    public static void drawRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                int colorTL, int colorTR, int colorBL, int colorBR) {
        float x1 = x0 + w, y1 = y0 + h;
        MuiGeometry.quads(graphics)
                .vertex(x0, y0).color(colorTL)
                .vertex(x0, y1).color(colorBL)
                .vertex(x1, y1).color(colorBR)
                .vertex(x1, y0).color(colorTR)
                .submit();
    }

    public static void drawRectRaw(MuiGeometry geo, float x0, float y0, float x1, float y1, int color) {
        geo.vertex(x0, y0).color(color)
                .vertex(x0, y1).color(color)
                .vertex(x1, y1).color(color)
                .vertex(x1, y0).color(color);
    }

    public static void drawRectRaw(MuiGeometry geo, float x0, float y0, float x1, float y1,
                                   int r, int g, int b, int a) {
        drawRectRaw(geo, x0, y0, x1, y1, Color.argb(r, g, b, a));
    }

    public static void drawCircle(GuiGraphicsExtractor graphics, float x0, float y0, float diameter, int color, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, color, color, segments);
    }

    public static void drawCircle(GuiGraphicsExtractor graphics, float x0, float y0, float diameter,
                                  int centerColor, int outerColor, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, centerColor, outerColor, segments);
    }

    public static void drawEllipse(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                   int color, int segments) {
        drawEllipse(graphics, x0, y0, w, h, color, color, segments);
    }

    public static void drawEllipse(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                   int centerColor, int outerColor, int segments) {
        MuiGeometry geo = MuiGeometry.fan(graphics);
        float x_2 = x0 + w / 2f, y_2 = y0 + h / 2f;
        // start at center
        geo.vertex(x_2, y_2).color(centerColor);
        float incr = TWO_PI / segments;
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = Mth.sin(angle) * (w / 2) + x_2;
            float y = Mth.cos(angle) * (h / 2) + y_2;
            geo.vertex(x, y).color(outerColor);
        }
        geo.submit();
    }

    public static void drawRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h, int color,
                                       int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, color, color, color, color, cornerRadius, segments);
    }

    public static void drawVerticalGradientRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                       int colorTop, int colorBottom, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom, cornerRadius, segments);
    }

    public static void drawHorizontalGradientRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                         int colorLeft, int colorRight, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight, cornerRadius, segments);
    }

    public static void drawRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                       int colorTL, int colorTR, int colorBL, int colorBR,
                                       int cornerRadius, int segments) {
        MuiGeometry geo = MuiGeometry.fan(graphics);
        float x1 = x0 + w, y1 = y0 + h;
        int color = Color.average(colorBL, colorBR, colorTR, colorTL);
        // start at center
        geo.vertex(x0 + w / 2f, y0 + h / 2f).color(color);
        // left side
        geo.vertex(x0, y0 + cornerRadius).color(colorTL);
        geo.vertex(x0, y1 - cornerRadius).color(colorBL);
        // bottom left corner
        for (int i = 1; i <= segments; i++) {
            float x = x0 + cornerRadius - Mth.cos(HALF_PI / segments * i) * cornerRadius;
            float y = y1 - cornerRadius + Mth.sin(HALF_PI / segments * i) * cornerRadius;
            geo.vertex(x, y).color(colorBL);
        }
        // bottom side
        geo.vertex(x1 - cornerRadius, y1).color(colorBR);
        // bottom right corner
        for (int i = 1; i <= segments; i++) {
            float x = x1 - cornerRadius + Mth.sin(HALF_PI / segments * i) * cornerRadius;
            float y = y1 - cornerRadius + Mth.cos(HALF_PI / segments * i) * cornerRadius;
            geo.vertex(x, y).color(colorBR);
        }
        // right side
        geo.vertex(x1, y0 + cornerRadius).color(colorTR);
        // top right corner
        for (int i = 1; i <= segments; i++) {
            float x = x1 - cornerRadius + Mth.cos(HALF_PI / segments * i) * cornerRadius;
            float y = y0 + cornerRadius - Mth.sin(HALF_PI / segments * i) * cornerRadius;
            geo.vertex(x, y).color(colorTR);
        }
        // top side
        geo.vertex(x0 + cornerRadius, y0).color(colorTL);
        // top left corner
        for (int i = 1; i <= segments; i++) {
            float x = x0 + cornerRadius - Mth.sin(HALF_PI / segments * i) * cornerRadius;
            float y = y0 + cornerRadius - Mth.cos(HALF_PI / segments * i) * cornerRadius;
            geo.vertex(x, y).color(colorTL);
        }
        geo.vertex(x0, y0 + cornerRadius).color(colorTL);
        geo.submit();
    }

    // ---------- textures ----------

    private static TextureAtlas guiAtlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
    }

    /**
     * Binds the texture differently depending on whether it is part of the GUI atlas.
     * <p>
     * If the texture is part of the GUI atlas, the atlas is bound and the UV coordinates are scaled to it.<br>
     * Otherwise the texture file itself is bound and the coordinates are returned unchanged.
     *
     * @param location the texture <i>file's</i> location, e.g. {@code "modularui:textures/gui/slot/item.png"}
     */
    public static RectangleF setupTexture(Identifier location, float u0, float v0, float u1, float v1) {
        TextureAtlasSprite sprite = guiAtlas().getSprite(GUI_TEXTURE_ID_CONVERTER.fileToId(location));

        // check if the atlas doesn't have this sprite, default to using the resloc as is if so
        if (!sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            bindTexture(sprite.atlasLocation());
            return new RectangleF(sprite.getU(u0), sprite.getV(v0), sprite.getU(u1), sprite.getV(v1));
        } else {
            bindTexture(location);
            return new RectangleF(u0, v0, u1, v1);
        }
    }

    public static boolean isGuiAtlasSprite(Identifier location) {
        TextureAtlasSprite sprite = guiAtlas().getSprite(GUI_TEXTURE_ID_CONVERTER.fileToId(location));
        return !sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location, float x, float y, float w,
                                   float h, int u, int v, int textureWidth, int textureHeight) {
        if (!isGuiAtlasSprite(location)) {
            bindTexture(location);
            drawTexture(graphics, x, y, u, v, w, h, textureWidth, textureHeight);
            return;
        }

        float tw = 1F / textureWidth;
        float th = 1F / textureHeight;
        RectangleF newUvs = setupTexture(location, u * tw, v * th, (u + w) * tw, (v + h) * th);

        drawTexture(graphics, x, y, x + w, y + h, newUvs.u0(), newUvs.v0(), newUvs.u1(), newUvs.v1());
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, float x, float y, int u, int v, float w, float h,
                                   int textureW, int textureH) {
        drawTexture(graphics, x, y, u, v, w, h, textureW, textureH, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(GuiGraphicsExtractor graphics, float x, float y, int u, int v, float w, float h,
                                   int textureW, int textureH, float z) {
        MuiGeometry geo = texturedQuads(graphics);
        drawTexture(geo, x, y, u, v, w, h, textureW, textureH, z);
        geo.submit();
    }

    public static void drawTexture(MuiGeometry geo, float x, float y, int u, int v,
                                   float w, float h, int textureW, int textureH, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        geo.vertex(x, y + h).uv(u * tw, (v + h) * th);
        geo.vertex(x + w, y + h).uv((u + w) * tw, (v + h) * th);
        geo.vertex(x + w, y).uv((u + w) * tw, v * th);
        geo.vertex(x, y).uv(u * tw, v * th);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, float x, float y, int u, int v, float w, float h,
                                   int textureW, int textureH, int tu, int tv) {
        drawTexture(graphics, x, y, u, v, w, h, textureW, textureH, tu, tv, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(GuiGraphicsExtractor graphics, float x, float y, int u, int v, float w, float h,
                                   int textureW, int textureH, int tu, int tv, float z) {
        MuiGeometry geo = texturedQuads(graphics);
        drawTexture(geo, x, y, u, v, w, h, textureW, textureH, tu, tv, z);
        geo.submit();
    }

    public static void drawTexture(MuiGeometry geo, float x, float y, int u, int v,
                                   float w, float h, int textureW, int textureH, int tu, int tv, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        geo.vertex(x, y + h).uv(u * tw, tv * th);
        geo.vertex(x + w, y + h).uv(tu * tw, tv * th);
        geo.vertex(x + w, y).uv(tu * tw, v * th);
        geo.vertex(x, y).uv(u * tw, v * th);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location, float x0, float y0, float x1,
                                   float y1, float u0, float v0, float u1, float v1) {
        drawTexture(graphics, location, x0, y0, x1, y1, u0, v0, u1, v1, false);
    }

    /** Blending is part of the GUI pipelines in 26.x, so {@code withBlend} no longer changes anything. */
    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location, float x0, float y0, float x1,
                                   float y1, float u0, float v0, float u1, float v1, boolean withBlend) {
        RectangleF newUvs = setupTexture(location, u0, v0, u1, v1);
        drawTexture(graphics, x0, y0, x1, y1, newUvs.u0(), newUvs.v0(), newUvs.u1(), newUvs.v1(), 0);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1,
                                   float u0, float v0, float u1, float v1) {
        drawTexture(graphics, x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1,
                                   float u0, float v0, float u1, float v1, float z) {
        MuiGeometry geo = texturedQuads(graphics);
        drawTexture(geo, x0, y0, x1, y1, u0, v0, u1, v1, z);
        geo.submit();
    }

    public static void drawTexture(MuiGeometry geo, float x0, float y0, float x1, float y1,
                                   float u0, float v0, float u1, float v1, float z) {
        geo.vertex(x0, y1).uv(u0, v1);
        geo.vertex(x1, y1).uv(u1, v1);
        geo.vertex(x1, y0).uv(u1, v0);
        geo.vertex(x0, y0).uv(u0, v0);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, Identifier location, float x, float y, float w,
                                        float h, int u, int v, int tileWidth, int tileHeight, int textureWidth,
                                        int textureHeight, float z) {
        if (!isGuiAtlasSprite(location)) {
            bindTexture(location);
            drawTiledTexture(graphics, x, y, w, h, u, v, tileWidth, tileHeight, textureWidth, textureHeight, z);
            return;
        }

        float wRatio = 1f / textureWidth;
        float hRatio = 1f / textureHeight;
        RectangleF newUvs = setupTexture(location, u * wRatio, v * hRatio, (u + w) * wRatio, (v + h) * hRatio);

        drawTiledTexture(graphics, x, y, x + w, y + h, newUvs.u0(), newUvs.v0(),
                newUvs.u1(), newUvs.v1(), tileWidth, tileHeight, z);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, float x, float y, float w, float h,
                                        int u, int v, int tileW, int tileH, int tw, int th, float z) {
        int countX = (((int) w - 1) / tileW) + 1;
        int countY = (((int) h - 1) / tileH) + 1;
        float fillerX = w - (countX - 1) * tileW;
        float fillerY = h - (countY - 1) * tileH;

        MuiGeometry geo = texturedQuads(graphics);
        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileW;
            float yy = y + iy * tileH;
            float xw = ix == countX - 1 ? fillerX : tileW;
            float yh = iy == countY - 1 ? fillerY : tileH;

            drawTexture(geo, xx, yy, u, v, xw, yh, tw, th, z);
        }
        geo.submit();
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, Identifier location, float x, float y, float w,
                                        float h, float u0, float v0, float u1, float v1,
                                        int textureWidth, int textureHeight, float z) {
        RectangleF newUvs = setupTexture(location, u0, v0, u1, v1);
        drawTiledTexture(graphics, x, y, w, h, newUvs.u0(), newUvs.v0(), newUvs.u1(), newUvs.v1(), textureWidth,
                textureHeight, z);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, float x, float y, float w, float h,
                                        float u0, float v0, float u1, float v1,
                                        int tileWidth, int tileHeight, float z) {
        int countX = (((int) w - 1) / tileWidth) + 1;
        int countY = (((int) h - 1) / tileHeight) + 1;
        float fillerX = w - (countX - 1) * tileWidth;
        float fillerY = h - (countY - 1) * tileHeight;
        float fillerU = u0 + (u1 - u0) * fillerX / tileWidth;
        float fillerV = v0 + (v1 - v0) * fillerY / tileHeight;

        MuiGeometry geo = texturedQuads(graphics);
        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileWidth;
            float yy = y + iy * tileHeight;
            float xw = tileWidth, yh = tileHeight, uEnd = u1, vEnd = v1;
            if (ix == countX - 1) {
                xw = fillerX;
                uEnd = fillerU;
            }
            if (iy == countY - 1) {
                yh = fillerY;
                vEnd = fillerV;
            }

            drawTexture(geo, xx, yy, xx + xw, yy + yh, u0, v0, uEnd, vEnd, z);
        }
        geo.submit();
    }

    // ---------- entities and items ----------

    /**
     * Draws an entity scaled into the given box. 26.x renders GUI entities as a picture-in-picture from their render
     * state, so pre/post draw callbacks run around the render state extraction, which is when they can still affect
     * the result.
     *
     * @param graphics the current {@link GuiGraphicsExtractor} instance.
     * @param entity   entity to draw
     * @param x        x pos
     * @param y        y pos
     * @param w        the width of the area where the entity should be drawn
     * @param h        the height of the area where the entity should be drawn
     * @param z        ignored; kept for compatibility
     * @param preDraw  a function to call before rendering. Transform or modify the entity here.
     * @param postDraw a function to call after rendering. Restore old entity state here if needed.
     * @param <T>      type of the entity to render
     */
    public static <T extends Entity> void drawEntity(GuiGraphicsExtractor graphics, T entity,
                                                     float x, float y, float w, float h, float z,
                                                     @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                     @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        drawEntityLookingAtAngle(graphics, entity, x, y, w, h, z, 0, 0, preDraw, postDraw);
    }

    /**
     * Draws an entity which looks in the direction of the mouse, like the player in the inventory.
     */
    public static <T extends Entity> void drawEntityLookingAtMouse(GuiGraphicsExtractor graphics, T entity,
                                                                   float x, float y, float w, float h, float z,
                                                                   int mouseX, int mouseY,
                                                                   @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                                   @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        float xAngle = (float) Math.atan((x + w / 2 - mouseX) / h);
        float yAngle = (float) Math.atan((y + h / 2 - mouseY) / h);
        drawEntityLookingAtAngle(graphics, entity, x, y, w, h, z, xAngle, yAngle, preDraw, postDraw);
    }

    /**
     * Draws an entity which looks toward a specific angle, following
     * {@link net.minecraft.client.gui.screens.inventory.InventoryScreen#extractEntityInInventoryFollowsMouse}.
     */
    public static <T extends Entity> void drawEntityLookingAtAngle(GuiGraphicsExtractor graphics, T entity,
                                                                   float x, float y, float w, float h, float z,
                                                                   float xAngle, float yAngle,
                                                                   @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                                   @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        if (preDraw != null) preDraw.accept(graphics, entity);
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        @SuppressWarnings("unchecked")
        EntityRenderer<? super T, ?> renderer = (EntityRenderer<? super T, ?>) dispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        if (postDraw != null) postDraw.accept(graphics, entity);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * Mth.DEG_TO_RAD);
        rotation.mul(xRotation);
        if (renderState instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + xAngle * 20.0F;
            living.yRot = xAngle * 20.0F;
            living.xRot = -yAngle * 20.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }

        // scale the entity so it fits right in the box
        float size = Math.max(entity.getBbHeight(), 0.01f);
        float scale = h / entity.getBbHeight() < w / entity.getBbWidth() ? h / size :
                w / Math.max(entity.getBbWidth(), 0.01f);
        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F, 0.0F);
        graphics.entity(renderState, scale, translation, rotation, xRotation,
                (int) x, (int) y, (int) (x + w), (int) (y + h));
    }

    public static void drawItem(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, float width, float height, int z) {
        if (item.isEmpty()) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width / 16f, height / 16f);
        graphics.item(item, 0, 0);
        graphics.itemDecorations(Minecraft.getInstance().font, item, 0, 0);
        graphics.pose().popMatrix();
    }

    public static void drawFluidTexture(GuiGraphicsExtractor graphics, FluidStack content,
                                        float x0, float y0, float width, float height, float z) {
        if (content == null || content.isEmpty()) {
            return;
        }
        Fluid fluid = content.getFluid();
        Identifier fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture(content);
        TextureAtlas blocks = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        TextureAtlasSprite sprite = blocks.getSprite(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(content);
        bindTexture(sprite.atlasLocation());
        setColor(fluidColor);
        drawTiledTexture(graphics, x0, y0, width, height, sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(), sprite.contents().width(), sprite.contents().height(), z);
        resetColor();
    }

    public static void drawStandardSlotAmountText(GuiContext context, int amount, String format, Area area,
                                                  float z) {
        drawAmountText(context.getMuiContext(), amount, format, 0, 0, area.width, area.height, Alignment.BottomRight, z);
    }

    public static void drawAmountText(ModularGuiContext context, int amount, String format,
                                      int x, int y, int width, int height, Alignment alignment, float z) {
        if (amount <= 1) return;
        String amountText = NumberFormat.AMOUNT_TEXT.format(amount);
        if (format != null) {
            amountText = format + amountText;
        }
        drawScaledAlignedTextInBox(context, amountText, x, y, width, height, alignment, 1f, z);
    }

    public static void drawScaledAlignedTextInBox(ModularGuiContext context, String amountText,
                                                  int x, int y, int width, int height, Alignment alignment) {
        drawScaledAlignedTextInBox(context, amountText, x, y, width, height, alignment, 1f, 0.0f);
    }

    public static void drawScaledAlignedTextInBox(ModularGuiContext context, String amountText,
                                                  int x, int y, int width, int height,
                                                  Alignment alignment, float maxScale, float z) {
        if (amountText == null || amountText.isEmpty()) return;
        // render the amount overlay
        textRenderer.setShadow(true);
        textRenderer.setScale(1f);
        textRenderer.setColor(Color.WHITE.main);
        textRenderer.setAlignment(alignment, width, height);
        textRenderer.setPos(x, y);
        textRenderer.setHardWrapOnBorder(false);
        if (amountText.length() > 2 && width > 16) { // we know that numbers below 100 will always fit in standard slots
            // simulate and calculate scale with width
            textRenderer.setSimulate(true);
            textRenderer.draw(context.getGraphics(), amountText);
            textRenderer.setSimulate(false);
            textRenderer.setScale(Math.min(maxScale, width / textRenderer.getLastWidth()));
        }
        // 26.x GUI layers follow submission order, so drawing last keeps the text on top
        textRenderer.draw(context.getGraphics(), amountText);
        textRenderer.setHardWrapOnBorder(true);
    }

    public static void drawSprite(GuiGraphicsExtractor graphics, TextureAtlasSprite sprite, float x0, float y0,
                                  float w, float h) {
        bindTexture(sprite.atlasLocation());
        drawTexture(graphics, x0, y0, x0 + w, y0 + h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void drawTiledSprite(GuiGraphicsExtractor graphics, TextureAtlasSprite sprite, float x0, float y0,
                                       float w, float h) {
        bindTexture(sprite.atlasLocation());
        drawTiledTexture(graphics, x0, y0, w, h, sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(), sprite.contents().width(), sprite.contents().height(), 0);
    }

    // ---------- outlines and borders ----------

    public static void drawOutlineCenter(GuiGraphicsExtractor graphics, int x, int y, int offset, int color) {
        drawOutlineCenter(graphics, x, y, offset, color, 1);
    }

    public static void drawOutlineCenter(GuiGraphicsExtractor graphics, int x, int y, int offset, int color, int border) {
        drawOutline(graphics, x - offset, y - offset, x + offset, y + offset, color, border);
    }

    public static void drawOutline(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        drawOutline(graphics, left, top, right, bottom, color, 1);
    }

    /**
     * Draw rectangle outline with given border
     */
    public static void drawOutline(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom,
                                   int color, int border) {
        graphics.fill(left, top, left + border, bottom, color);
        graphics.fill(right - border, top, right, bottom, color);
        graphics.fill(left + border, top, right - border, top + border, color);
        graphics.fill(left + border, bottom - border, right - border, bottom, color);
    }

    private static void drawBorderLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                       float border, int color, boolean outside) {
        if (outside) {
            left -= border;
            top -= border;
            right += border;
            bottom += border;
        }
        float x0 = left, y0 = top, x1 = right, y1 = bottom, d = border;

        MuiGeometry.strip(graphics)
                .vertex(x0, y0).color(color)
                .vertex(x1 - d, y0 + d).color(color)
                .vertex(x1, y0).color(color)
                .vertex(x1 - d, y1 - d).color(color)
                .vertex(x1, y1).color(color)
                .vertex(x0 + d, y1 - d).color(color)
                .vertex(x0, y1).color(color)
                .vertex(x0 + d, y0 + d).color(color)
                .vertex(x0, y0).color(color)
                .vertex(x1 - d, y0 + d).color(color)
                .submit();
    }

    public static void drawBorderOutsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                             int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, 1, color, true);
    }

    public static void drawBorderOutsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                             float border, int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, border, color, true);
    }

    public static void drawBorderInsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                            int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, 1, color, false);
    }

    public static void drawBorderInsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                            float border, int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, border, color, false);
    }

    private static void drawBorderXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                       int color, boolean outside) {
        drawBorderLTRB(graphics, x, y, x + w, y + h, border, color, outside);
    }

    public static void drawBorderOutsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                             int color) {
        drawBorderXYWH(graphics, x, y, w, h, border, color, true);
    }

    public static void drawBorderOutsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        drawBorderXYWH(graphics, x, y, w, h, 1, color, true);
    }

    public static void drawBorderInsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                            int color) {
        drawBorderXYWH(graphics, x, y, w, h, border, color, false);
    }

    public static void drawBorderInsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        drawBorderXYWH(graphics, x, y, w, h, 1, color, false);
    }

    // ---------- shadows ----------

    /**
     * Draws a rectangular shadow
     *
     * @param x      left of solid shadow part
     * @param y      top of solid shadow part
     * @param w      width of solid shadow part
     * @param h      height of solid shadow part
     * @param oX     shadow gradient size in x
     * @param oY     shadow gradient size in y
     * @param opaque solid shadow color
     * @param shadow gradient end color
     */
    public static void drawDropShadow(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int oX, int oY,
                                      int opaque, int shadow) {
        float x1 = x + w, y1 = y + h;
        MuiGeometry.quads(graphics)
                /* Draw opaque part */
                .vertex(x1, y).color(opaque).vertex(x, y).color(opaque)
                .vertex(x, y1).color(opaque).vertex(x1, y1).color(opaque)
                /* Draw top shadow */
                .vertex(x1 + oX, y - oY).color(shadow).vertex(x - oX, y - oY).color(shadow)
                .vertex(x, y).color(opaque).vertex(x1, y).color(opaque)
                /* Draw bottom shadow */
                .vertex(x1, y1).color(opaque).vertex(x, y1).color(opaque)
                .vertex(x - oX, y1 + oY).color(shadow).vertex(x1 + oX, y1 + oY).color(shadow)
                /* Draw left shadow */
                .vertex(x, y).color(opaque).vertex(x - oX, y - oY).color(shadow)
                .vertex(x - oX, y1 + oY).color(shadow).vertex(x, y1).color(opaque)
                /* Draw right shadow */
                .vertex(x1 + oX, y - oY).color(shadow).vertex(x1, y).color(opaque)
                .vertex(x1, y1).color(opaque).vertex(x1 + oX, y1 + oY).color(shadow)
                .submit();
    }

    public static void drawDropCircleShadow(GuiGraphicsExtractor graphics, int x, int y, int radius, int segments,
                                            int opaque, int shadow) {
        MuiGeometry geo = MuiGeometry.fan(graphics);
        geo.vertex(x, y).color(opaque);
        for (int i = 0; i <= segments; i++) {
            float a = i / (float) segments * TWO_PI - HALF_PI;
            circleVertex(geo, x, Mth.cos(a), y, Mth.sin(a), radius).color(shadow);
        }
        geo.submit();
    }

    public static void drawDropCircleShadow(GuiGraphicsExtractor graphics, int x, int y, int radius, int offset, int segments,
                                            int opaque, int shadow) {
        if (offset >= radius) {
            drawDropCircleShadow(graphics, x, y, radius, segments, opaque, shadow);
            return;
        }
        /* Draw opaque base */
        MuiGeometry geo = MuiGeometry.fan(graphics);
        geo.vertex(x, y).color(opaque);
        for (int i = 0; i <= segments; i++) {
            float a = i / (float) segments * TWO_PI - HALF_PI;
            circleVertex(geo, x, Mth.cos(a), y, Mth.sin(a), offset).color(opaque);
        }
        geo.submit();

        /* Draw outer shadow */
        geo = MuiGeometry.quads(graphics);
        for (int i = 0; i < segments; i++) {
            float alpha1 = i / (float) segments * TWO_PI - HALF_PI;
            float alpha2 = (i + 1) / (float) segments * TWO_PI - HALF_PI;

            float cosA1 = Mth.cos(alpha1);
            float cosA2 = Mth.cos(alpha2);
            float sinA1 = Mth.sin(alpha1);
            float sinA2 = Mth.sin(alpha2);

            circleVertex(geo, x, cosA2, y, sinA2, offset).color(opaque);
            circleVertex(geo, x, cosA1, y, sinA1, offset).color(opaque);
            circleVertex(geo, x, cosA1, y, sinA1, radius).color(shadow);
            circleVertex(geo, x, cosA2, y, sinA2, radius).color(shadow);
        }
        geo.submit();
    }

    private static MuiGeometry circleVertex(MuiGeometry geo, float x, float xOffset, float y, float yOffset,
                                            float mul) {
        return geo.vertex(x - xOffset * mul, y + yOffset * mul);
    }

    @Environment(EnvType.CLIENT)
    public static void drawBorder(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color,
                                  float border) {
        drawBorderLTRB(graphics, x, y, x + width, y + height, border, color, false);
    }

    // ---------- text ----------

    @Environment(EnvType.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, String text, float x, float y,
                                float scale, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(Minecraft.getInstance().font, text, 0, 0, TextColors.legacy(color), shadow);
        graphics.pose().popMatrix();
    }

    @Environment(EnvType.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, Component text, float x, float y, float scale,
                                int color, boolean shadow) {
        drawText(graphics, text.getVisualOrderText(), x, y, scale, color, shadow);
    }

    @Environment(EnvType.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, FormattedCharSequence text, float x, float y, float scale,
                                int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(Minecraft.getInstance().font, text, 0, 0, TextColors.legacy(color), shadow);
        graphics.pose().popMatrix();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void drawTooltipBackground(GuiContext context, ItemStack stack, List<ClientTooltipComponent> lines,
                                             int x, int y, int textWidth, int height, @Nullable IRichTextBuilder<?> tooltip) {
        GuiGraphicsExtractor graphics = context.getGraphics();

        // TODO theme color
        int backgroundTop = 0xF0100010;
        int backgroundBottom = backgroundTop;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent;

        if (tooltip != null) {
            colorEvent = new RichTooltipEvent.Color(stack, graphics, x, y, context.getFont(),
                    backgroundTop, borderColorStart, borderColorEnd, lines, tooltip);
        } else {
            colorEvent = new RenderTooltipEvent.Color(stack, graphics, x, y, context.getFont(),
                    backgroundTop, borderColorStart, borderColorEnd, lines);
        }

        NeoForge.EVENT_BUS.post(colorEvent);
        backgroundTop = colorEvent.getBackgroundStart();
        backgroundBottom = colorEvent.getBackgroundEnd();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        // top background border
        drawVerticalGradientRect(graphics, x - 3, y - 4, textWidth + 6, 1, backgroundTop, backgroundTop);
        // bottom background border
        drawVerticalGradientRect(graphics, x - 3, y + height + 3, textWidth + 6, 1, backgroundBottom, backgroundBottom);
        // center background
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, height + 6, backgroundTop, backgroundBottom);
        // left background border
        drawVerticalGradientRect(graphics, x - 4, y - 3, 1, height + 6, backgroundTop, backgroundBottom);
        // right background border
        drawVerticalGradientRect(graphics, x + textWidth + 3, y - 3, 1, height + 6, backgroundTop, backgroundBottom);

        // left accent border
        drawVerticalGradientRect(graphics, x - 3, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // right accent border
        drawVerticalGradientRect(graphics, x + textWidth + 2, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // top accent border
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, 1, borderColorStart, borderColorStart);
        // bottom accent border
        drawVerticalGradientRect(graphics, x - 3, y + height + 2, textWidth + 6, 1, borderColorEnd, borderColorEnd);
    }
}
