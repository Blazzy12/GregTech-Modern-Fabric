package com.gregtechceu.gtceu.compat.common;

import com.gregtechceu.gtceu.GTCEu;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * The parts of NeoForge's own mod content that GT uses. Attributes live under {@code gtceu:}; they are added to every
 * living entity by {@code LivingEntityAttributesMixin} and applied by the player/swim mixins.
 */
public final class NeoForgeMod {

    private NeoForgeMod() {}

    /** Values above 0 let a player fly as in creative mode. */
    public static final Holder<Attribute> CREATIVE_FLIGHT = registerAttribute("creative_flight",
            new RangedAttribute("attribute.name.gtceu.creative_flight", 0.0, 0.0, 1.0).setSyncable(true));

    /** Multiplier on swimming speed. */
    public static final Holder<Attribute> SWIM_SPEED = registerAttribute("swim_speed",
            new RangedAttribute("attribute.name.gtceu.swim_speed", 1.0, 0.0, 1024.0).setSyncable(true));

    /** Damage type of GT's poison effect; defined in {@code data/gtceu/damage_type/poison.json}. */
    public static final ResourceKey<DamageType> POISON_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            GTCEu.id("poison"));

    public static final AttachmentType<CompoundTag> PERSISTENT_DATA = AttachmentRegistry.<CompoundTag>builder()
            .persistent(CompoundTag.CODEC)
            .initializer(CompoundTag::new)
            .copyOnDeath()
            .buildAndRegister(GTCEu.id("persistent_data"));

    /**
     * The attributes are registered as soon as this class loads: the dedicated server builds the default entity
     * attributes (which GT adds these to) in {@code Bootstrap.validate}, before any mod initializer runs.
     */
    private static Holder<Attribute> registerAttribute(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, GTCEu.id(name), attribute);
    }

    /** Called from the common entrypoint, to load the class. */
    public static void init() {}
}
