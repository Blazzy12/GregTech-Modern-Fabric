package com.gregtechceu.gtceu.compat.fluids;

import com.gregtechceu.gtceu.compat.common.SoundAction;
import com.gregtechceu.gtceu.compat.common.SoundActions;
import com.gregtechceu.gtceu.core.fabric.FluidTypeHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Physical and display properties shared by the still and flowing variants of a fluid.
 * Every {@link Fluid} exposes its type through an injected {@code getFluidType()} method.
 */
public class FluidType {

    public static final int BUCKET_VOLUME = 1000;

    public static final FluidType EMPTY_TYPE = new FluidType(Properties.create()
            .descriptionId("block.minecraft.air").density(0).temperature(0).viscosity(0)) {

        @Override
        public boolean isAir() {
            return true;
        }
    };
    public static final FluidType WATER_TYPE = new FluidType(Properties.create()
            .descriptionId("block.minecraft.water").canExtinguish(true).canHydrate(true).canConvertToSource(true)
            .supportsBoating(true).canSwim(true).canDrown(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH));
    public static final FluidType LAVA_TYPE = new FluidType(Properties.create()
            .descriptionId("block.minecraft.lava").canSwim(false).canDrown(false).lightLevel(15).density(3000)
            .viscosity(6000).temperature(1300)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA));

    private final String descriptionId;
    private final int lightLevel, density, temperature, viscosity;
    private final Rarity rarity;
    private final boolean canSwim, canDrown, canExtinguish, canHydrate, canConvertToSource, supportsBoating,
            canPushEntity;
    private final double motionScale;
    private final float fallDistanceModifier;
    private final Map<SoundAction, SoundEvent> sounds;

    public FluidType(Properties properties) {
        this.descriptionId = properties.descriptionId;
        this.lightLevel = properties.lightLevel;
        this.density = properties.density;
        this.temperature = properties.temperature;
        this.viscosity = properties.viscosity;
        this.rarity = properties.rarity;
        this.canSwim = properties.canSwim;
        this.canDrown = properties.canDrown;
        this.canExtinguish = properties.canExtinguish;
        this.canHydrate = properties.canHydrate;
        this.canConvertToSource = properties.canConvertToSource;
        this.supportsBoating = properties.supportsBoating;
        this.canPushEntity = properties.canPushEntity;
        this.motionScale = properties.motionScale;
        this.fallDistanceModifier = properties.fallDistanceModifier;
        this.sounds = Map.copyOf(properties.sounds);
    }

    public static FluidType of(Fluid fluid) {
        return ((FluidTypeHolder) fluid).getFluidType();
    }

    public Component getDescription() {
        return Component.translatable(getDescriptionId());
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public Component getDescription(FluidStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }

    public String getDescriptionId(FluidStack stack) {
        return getDescriptionId();
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public int getLightLevel(FluidStack stack) {
        return getLightLevel();
    }

    public int getLightLevel(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getLightLevel();
    }

    public int getDensity() {
        return density;
    }

    public int getDensity(FluidStack stack) {
        return getDensity();
    }

    public int getTemperature() {
        return temperature;
    }

    public int getTemperature(FluidStack stack) {
        return getTemperature();
    }

    public int getViscosity() {
        return viscosity;
    }

    public int getViscosity(FluidStack stack) {
        return getViscosity();
    }

    public Rarity getRarity() {
        return rarity;
    }

    public Rarity getRarity(FluidStack stack) {
        return getRarity();
    }

    public @Nullable SoundEvent getSound(SoundAction action) {
        return sounds.get(action);
    }

    public @Nullable SoundEvent getSound(FluidStack stack, SoundAction action) {
        return getSound(action);
    }

    public @Nullable SoundEvent getSound(@Nullable Entity entity, SoundAction action) {
        return getSound(action);
    }

    public boolean canSwim(Entity entity) {
        return canSwim;
    }

    public boolean canDrownIn(Entity entity) {
        return canDrown;
    }

    public boolean canExtinguish(Entity entity) {
        return canExtinguish;
    }

    public boolean canHydrate(Entity entity) {
        return canHydrate;
    }

    public boolean canHydrate(FluidStack stack) {
        return canHydrate;
    }

    public boolean canConvertToSource(FluidStack stack) {
        return canConvertToSource;
    }

    public boolean canPushEntity(Entity entity) {
        return canPushEntity;
    }

    public boolean supportsBoating() {
        return supportsBoating;
    }

    public double motionScale(Entity entity) {
        return motionScale;
    }

    public float getFallDistanceModifier(Entity entity) {
        return fallDistanceModifier;
    }

    public boolean isAir() {
        return false;
    }

    public final boolean isVanilla() {
        return this == WATER_TYPE || this == LAVA_TYPE || this == EMPTY_TYPE;
    }

    public final boolean isLighterThanAir() {
        return density <= 0;
    }

    public ItemStack getBucket(FluidStack stack) {
        return new ItemStack(stack.getFluid().getBucket());
    }

    public BlockState getBlockForFluidState(BlockAndTintGetter getter, BlockPos pos, FluidState state) {
        return state.createLegacyBlock();
    }

    public FluidState getStateForPlacement(BlockAndTintGetter getter, BlockPos pos, FluidStack stack) {
        return stack.getFluid().defaultFluidState();
    }

    public boolean isVaporizedOnPlacement(net.minecraft.world.level.Level level, BlockPos pos, FluidStack stack) {
        return this == WATER_TYPE && level.environmentAttributes().getValue(
                net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, pos);
    }

    public void onVaporize(@Nullable net.minecraft.world.entity.player.Player player,
                           net.minecraft.world.level.Level level, BlockPos pos, FluidStack stack) {
        SoundEvent sound = getSound(stack, SoundActions.FLUID_VAPORIZE);
        level.playSound(player, pos, sound == null ? SoundEvents.FIRE_EXTINGUISH : sound,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5F,
                2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
    }

    /** Client properties are registered through {@code RegisterClientExtensionsEvent} instead. */
    @Deprecated
    public void initializeClient(Consumer<Object> consumer) {}

    @Override
    public String toString() {
        return "FluidType[" + descriptionId + "]";
    }

    /** Vanilla fluids that have no type assigned through {@link FluidTypeHolder}. */
    public static FluidType defaultFor(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return EMPTY_TYPE;
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) return LAVA_TYPE;
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return WATER_TYPE;
        return WATER_TYPE;
    }

    public static final class Properties {

        private String descriptionId = "";
        private int lightLevel = 0, density = 1000, temperature = 300, viscosity = 1000;
        private Rarity rarity = Rarity.COMMON;
        private boolean canSwim = true, canDrown = true, canExtinguish = false, canHydrate = false,
                canConvertToSource = false, supportsBoating = false, canPushEntity = true;
        private double motionScale = 0.014;
        private float fallDistanceModifier = 0.5F;
        private final Map<SoundAction, SoundEvent> sounds = new HashMap<>();

        private Properties() {}

        public static Properties create() {
            return new Properties();
        }

        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public Properties lightLevel(int lightLevel) {
            this.lightLevel = lightLevel;
            return this;
        }

        public Properties density(int density) {
            this.density = density;
            return this;
        }

        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public Properties viscosity(int viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Properties canSwim(boolean canSwim) {
            this.canSwim = canSwim;
            return this;
        }

        public Properties canDrown(boolean canDrown) {
            this.canDrown = canDrown;
            return this;
        }

        public Properties canExtinguish(boolean canExtinguish) {
            this.canExtinguish = canExtinguish;
            return this;
        }

        public Properties canHydrate(boolean canHydrate) {
            this.canHydrate = canHydrate;
            return this;
        }

        public Properties canConvertToSource(boolean canConvertToSource) {
            this.canConvertToSource = canConvertToSource;
            return this;
        }

        public Properties supportsBoating(boolean supportsBoating) {
            this.supportsBoating = supportsBoating;
            return this;
        }

        public Properties canPushEntity(boolean canPushEntity) {
            this.canPushEntity = canPushEntity;
            return this;
        }

        public Properties motionScale(double motionScale) {
            this.motionScale = motionScale;
            return this;
        }

        public Properties fallDistanceModifier(float fallDistanceModifier) {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        public Properties sound(SoundAction action, SoundEvent sound) {
            sounds.put(action, sound);
            return this;
        }
    }
}
