package com.gregtechceu.gtceu.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.compat.client.model.BakedQuad;

import net.minecraft.util.LightCoordsUtil;

import net.minecraft.util.RandomSource;

import net.minecraft.data.AtlasIds;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.gregtechceu.gtceu.compat.client.color.ColorHandlers;

import com.gregtechceu.gtceu.compat.client.model.VanillaBakedModel;

import com.gregtechceu.gtceu.compat.client.model.GTBlockStateModel;

import net.minecraft.client.renderer.texture.TextureAtlas;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.drawable.GuiDraw;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;
import java.util.function.BiFunction;

import static net.minecraft.util.ARGB.*;

@Environment(EnvType.CLIENT)
public class RenderUtil {

    /** @return a client level as the tint getter models and fluid renderers need, or an empty one */
    public static net.minecraft.client.renderer.block.BlockAndTintGetter tintGetter(
            @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level) {
        return level instanceof net.minecraft.client.renderer.block.BlockAndTintGetter getter ? getter :
                net.minecraft.client.renderer.block.BlockAndTintGetter.EMPTY;
    }


    public enum FluidTextureType {

        STILL((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getStillTexture(fluidStack);
            else return fluidTypeExtensions.getStillTexture();
        }),
        FLOWING((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getFlowingTexture(fluidStack);
            else return fluidTypeExtensions.getFlowingTexture();
        }),
        OVERLAY((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getOverlayTexture(fluidStack);
            else return fluidTypeExtensions.getOverlayTexture();
        });

        private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");

        private final BiFunction<IClientFluidTypeExtensions, FluidStack, Identifier> mapper;

        FluidTextureType(BiFunction<IClientFluidTypeExtensions, FluidStack, Identifier> mapper) {
            this.mapper = mapper;
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
            return map(fluidTypeExtensions, FluidStack.EMPTY);
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
            Identifier texture = mapper.apply(fluidTypeExtensions, fluidStack);
            if (texture == null) texture = STILL.mapper.apply(fluidTypeExtensions, fluidStack);
            if (texture == null) texture = WATER_STILL;

            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
        }
    }

    public static Vec3 vec3(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vector3f vec3f(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    // spotless:off
    private static final Map<Direction, Vector3fc[]> DIRECTION_POSITION_MAP = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, new Vector3fc[] { vec3f(0, 1, 1), vec3f(1, 1, 1), vec3f(1, 1, 0), vec3f(0, 1, 0) });
        map.put(Direction.DOWN, new Vector3fc[] { vec3f(1, 0, 1), vec3f(0, 0, 1), vec3f(0, 0, 0), vec3f(1, 0, 0) });
        map.put(Direction.SOUTH, new Vector3fc[] { vec3f(1, 1, 0), vec3f(1, 0, 0), vec3f(0, 0, 0), vec3f(0, 1, 0) });
        map.put(Direction.NORTH, new Vector3fc[] { vec3f(0, 1, 1), vec3f(0, 0, 1), vec3f(1, 0, 1), vec3f(1, 1, 1) });
        map.put(Direction.EAST, new Vector3fc[] { vec3f(0, 1, 0), vec3f(0, 0, 0), vec3f(0, 0, 1), vec3f(0, 1, 1) });
        map.put(Direction.WEST, new Vector3fc[] { vec3f(1, 1, 1), vec3f(1, 0, 1), vec3f(1, 0, 0), vec3f(1, 1, 0) });
    });
    // spotless:on

    public static Vector3fc[] getVertices(Direction direction) {
        return DIRECTION_POSITION_MAP.get(direction);
    }

    // spotless:off
    private static final Map<Direction, Vector3fc> DIRECTION_NORMAL_MAP = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, vec3f(0, 1, 0));
        map.put(Direction.DOWN, vec3f(0, 1, 0));
        map.put(Direction.SOUTH, vec3f(0, 0, 1));
        map.put(Direction.NORTH, vec3f(0, 0, 1));
        map.put(Direction.EAST, vec3f(1, 0, 0));
        map.put(Direction.WEST, vec3f(1, 0, 0));
    });
    // spotless:on

    public static Vector3fc getNormal(Direction direction) {
        return DIRECTION_NORMAL_MAP.get(direction);
    }

    public static int getFluidLight(Fluid fluid, BlockPos pos, @Nullable BlockAndTintGetter level) {
        if (level == null) level = Minecraft.getInstance().level;
        if (level == null) return 0;

        return LightCoordsUtil.getLightCoords(level, pos);
    }

    public static void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                              Vector3fc pos, Vector3fc normal, float u, float v,
                              int argb, int packedOverlay, int packedLight) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer
                .addVertex(pose, pos.x(), pos.y(), pos.z())
                .setColor(argb)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    public static void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                              float x, float y, float z,
                              int r, int g, int b, int a, float u, float v,
                              int packedOverlay, int packedLight,
                              float v0, float v1, float v2) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, v0, v1, v2);
    }

    public static Vector3f transformVertex(Vector3fc vertex, Direction direction,
                                           float offsetX, float offsetY, float offsetZ) {
        float addX = offsetX, addY = offsetY, addZ = offsetZ;
        switch (direction) {
            case DOWN -> addY = -addY;
            case SOUTH -> addZ = -addZ;
            case EAST -> addX = -addX;
        }

        return new Vector3f(vertex).add(addX, addY, addZ);
    }

    public static @Nullable Fluid getRecipeFluidToRender(GTRecipe recipe) {
        if (recipe == null) {
            return null;
        }
        var contents = new ObjectArrayList<Content>();
        var empty = new ArrayList<Content>();
        contents.addAll(recipe.outputs.getOrDefault(FluidRecipeCapability.CAP, empty));
        contents.addAll(recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, empty));
        if (contents.isEmpty()) {
            return null;
        }

        var fluidContent = contents.stream()
                .filter(content -> content.content() instanceof SizedFluidIngredient ingredient &&
                        !ingredient.ingredient().hasNoFluids())
                .findAny();
        if (fluidContent.isEmpty()) {
            return null;
        }
        var ingredient = (SizedFluidIngredient) fluidContent.get().content();

        var stacks = ingredient.getFluids();
        if (stacks.length == 0) {
            return null;
        }

        Fluid fluid = null;
        for (int i = 0; i < stacks.length && fluid == null; i++) {
            if (!stacks[i].isEmpty()) {
                fluid = stacks[i].getFluid();
            }
        }

        return fluid;
    }

    /**
     * Converts an (A)RGB integer color into an array of floats, for use in GL calls
     *
     * @return float[]{R, G, B, A}
     */
    public static float[] floats(int argb) {
        return new float[] {
                (float) (argb >> 16 & 255) / 255.0F,
                (float) (argb >> 8 & 255) / 255.0F,
                (float) (argb & 255) / 255.0F,
                (float) (argb >> 24 & 255) / 255.0F
        };
    }

    public static int interpolateColor(int color1, int color2, float blend) {
        int a1 = color1 >> 24 & 255;
        int r1 = color1 >> 16 & 255;
        int g1 = color1 >> 8 & 255;
        int b1 = color1 & 255;

        int a2 = color2 >> 24 & 255;
        int r2 = color2 >> 16 & 255;
        int g2 = color2 >> 8 & 255;
        int b2 = color2 & 255;

        int a = (int) (a1 * (1 - blend) + a2 * blend);
        int r = (int) (r1 * (1 - blend) + r2 * blend);
        int g = (int) (g1 * (1 - blend) + g2 * blend);
        int b = (int) (b1 * (1 - blend) + b2 * blend);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void moveToFace(PoseStack poseStack, Vector3fc pos, Direction face) {
        moveToFace(poseStack, pos.x(), pos.y(), pos.z(), face);
    }

    public static void moveToFace(PoseStack poseStack, float x, float y, float z, Direction face) {
        poseStack.translate(Math.fma(face.getStepX(), 0.5f, x),
                Math.fma(face.getStepY(), 0.5f, y),
                Math.fma(face.getStepZ(), 0.5f, z));
    }

    /** @return the model of a block state as a GregTech model */
    public static BakedModel getModelForState(BlockState state) {
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        return model instanceof GTBlockStateModel gtModel ? gtModel.model() : new VanillaBakedModel(model);
    }

    public static void drawBlock(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                 MultiBufferSource bufferSource, PoseStack poseStack) {
        int packedLight = LightCoordsUtil.getLightCoords(level, pos);
        BakedModel model = getModelForState(state);
        ModelData modelData = model.getModelData(level, pos, state, ModelData.EMPTY);
        RandomSource random = RandomSource.create(state.getSeed(pos));
        for (var layer : model.getRenderTypes(state, random, modelData)) {
            VertexConsumer buffer = bufferSource.getBuffer(switch (layer) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            });
            for (Direction side : BakedModel.SIDES) {
                for (BakedQuad quad : model.getQuads(state, side, random, modelData, layer)) {
                    int color = quad.isTinted() ?
                            ColorHandlers.getBlockColor(state, level, pos, quad.getTintIndex()) : -1;
                    quad.putInto(buffer, poseStack.last(), color, packedLight, OverlayTexture.NO_OVERLAY);
                }
            }
        }
    }

    /**
     * Rotate the current coordinate system, so it is on the face of the given block side.
     * This can be used to render on the given face as if it was a 2D canvas,
     * where x+ is facing right and y+ is facing up.
     */
    public static void rotateToFace(PoseStack poseStack, Direction face, Direction spin) {
        float rotationAngle = Mth.HALF_PI * switch (face) {
            case UP, WEST -> 1;
            case DOWN, EAST -> -1;
            case SOUTH -> 2;
            case NORTH -> 0;
        };
        Quaternionf rotation = new Quaternionf();
        if (face.getAxis() == Direction.Axis.Y) {
            poseStack.scale(1.0f, -1.0f, 1.0f);
            rotation.rotateX(rotationAngle);
        } else {
            poseStack.scale(-1.0f, -1.0f, -1.0f);
            rotation.rotateY(rotationAngle);
        }
        rotation.rotateZ(getSpinAngle(spin, face));

        poseStack.rotate(rotation);
    }

    private static float getSpinAngle(Direction spin, Direction face) {
        if (spin.getAxis() == Direction.Axis.Z && face == Direction.DOWN) {
            spin = spin.getOpposite();
        }
        return GTMatrixUtils.upwardFacingAngle(spin);
    }

    public static boolean renderResearchItemContent(GuiGraphicsExtractor graphics, Operation<Void> originalMethod,
                                                    @Nullable LivingEntity entity, @Nullable Level level,
                                                    ItemStack stack, int x, int y, int seed) {
        if (!net.minecraft.client.Minecraft.getInstance().hasShiftDown()) return false;

        ResearchManager.ResearchItem researchData = stack.get(GTDataComponents.RESEARCH_ITEM);
        if (researchData == null) return false;

        Collection<GTRecipe> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
        if (recipes == null || recipes.isEmpty()) return false;

        for (var recipe : recipes) {
            // check item outputs first
            List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                ItemStack[] items = ItemRecipeCapability.CAP.of(outputs.getFirst().content()).getItems();
                if (items.length > 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, stack)) {
                        originalMethod.call(entity, level, output, x, y, seed);
                        return true;
                    }
                }
            }
            // if there are no item outputs, try to find a fluid output
            outputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                FluidStack[] fluids = FluidRecipeCapability.CAP.of(outputs.getFirst().content()).getFluids();
                if (fluids.length != 0) {
                    FluidStack output = fluids[0];
                    if (!output.isEmpty()) {
                        GuiDraw.drawFluidTexture(graphics, output, x, y, 0, 0, 0);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
