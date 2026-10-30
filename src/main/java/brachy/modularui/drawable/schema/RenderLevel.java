package brachy.modularui.drawable.schema;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * The view of a schema's level used for tesselation and ray tracing, hiding blocks that are filtered out.
 */
public class RenderLevel implements BlockAndTintGetter {

    @Getter private final ISchema schema;
    private final Level level;
    private final RenderFilter renderFilter;
    private final Thread thread;

    public RenderLevel(ISchema schema, RenderFilter renderFilter) {
        this.schema = schema;
        this.level = schema.getLevel();
        this.renderFilter = renderFilter;

        this.thread = Thread.currentThread();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.renderFilter.shouldRender(pos, state)) {
            return null;
        }
        // avoid the level
        if (Thread.currentThread() != this.thread) {
            int chunkX = SectionPos.blockToSectionCoord(pos.getX());
            int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
            var chunk = this.level.getChunkForCollisions(chunkX, chunkZ);
            if (chunk == null) {
                return null;
            }
            return chunk.getBlockEntity(pos);
        }
        return this.level.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.renderFilter.shouldRender(pos, state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.renderFilter.shouldRender(pos, state)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.level.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        return color.getColor(this.level.getBiome(pos).value(), pos.getX(), pos.getZ());
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }
}
