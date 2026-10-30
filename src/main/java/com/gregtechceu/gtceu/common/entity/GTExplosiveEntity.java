package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.core.mixins.PrimedTntAccessor;

import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTExplosiveEntity extends PrimedTnt {

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type,
                             Level level, double x, double y, double z,
                             @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        double d = level.getRandom().nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        ((PrimedTntAccessor) this).setOwner(net.minecraft.world.entity.EntityReference.of(owner));
    }

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @return The strength of the explosive.
     */
    protected abstract float getStrength();

    /**
     * @return Whether to drop all blocks, or use default logic
     */
    public abstract boolean dropsAllBlocks();

    /**
     * @return The range of the explosive, if {@link #dropsAllBlocks} is true.
     */
    protected int getRange() {
        return 2;
    }

    /**
     * @return The block state of the block this explosion entity is created by.
     */
    public abstract @NotNull BlockState getExplosiveState();

    /**
     * Same as {@link PrimedTnt#tick()}, which calls a private {@code explode()} in 26.x that cannot be overridden.
     */
    @Override
    public void tick() {
        this.handlePortal();
        this.applyGravity();
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.setDeltaMovement(this.getDeltaMovement().scale(this.getAirDrag()));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }

        int fuse = this.getFuse() - 1;
        this.setFuse(fuse);
        if (fuse <= 0) {
            this.discard();
            if (!this.level().isClientSide()) {
                this.explode();
            }
        } else {
            this.updateFluidInteraction();
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0,
                        0.0);
            }
        }

        this.setRequiresPrecisePosition(this.horizontalCollision || this.verticalCollision);
    }

    protected void explode() {
        explode(level(), this, this.getX(), this.getY(0.0625), this.getZ(), getStrength(), dropsAllBlocks());
    }

    protected void explode(Level level, @Nullable Entity source, double x, double y, double z,
                           float radius, boolean dropBlocks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 center = new Vec3(x, y, z);
        // what ServerLevel#explode does, with GT's choice of block interaction
        ServerExplosion explosion = new ServerExplosion(serverLevel, source,
                Explosion.getDefaultDamageSource(level, source), null, center, radius, false,
                dropBlocks ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY);
        int blockCount = explosion.explode();
        ParticleOptions particle = explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;

        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(center) < 4096.0) {
                Optional<Vec3> knockback = Optional.ofNullable(explosion.getHitPlayers().get(player));
                player.connection.send(new ClientboundExplodePacket(center, radius, blockCount, knockback, particle,
                        SoundEvents.GENERIC_EXPLODE, Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES,
                        source == null || !source.isSilent()));
            }
        }
    }
}
