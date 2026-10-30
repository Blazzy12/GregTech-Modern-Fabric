package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonPrimitive;

public final class VariantProperties {

    public static final VariantProperty<Rotation> X_ROT = new VariantProperty<>("x", r -> new JsonPrimitive(r.value));
    public static final VariantProperty<Rotation> Y_ROT = new VariantProperty<>("y", r -> new JsonPrimitive(r.value));
    /** Native in 26.x blockstate variants. */
    public static final VariantProperty<Rotation> Z_ROT = new VariantProperty<>("z", r -> new JsonPrimitive(r.value));
    public static final VariantProperty<Identifier> MODEL = new VariantProperty<>("model",
            id -> new JsonPrimitive(id.toString()));
    public static final VariantProperty<Boolean> UV_LOCK = new VariantProperty<>("uvlock", JsonPrimitive::new);
    public static final VariantProperty<Integer> WEIGHT = new VariantProperty<>("weight", JsonPrimitive::new);

    private VariantProperties() {}

    public enum Rotation {

        R0(0),
        R90(90),
        R180(180),
        R270(270);

        final int value;

        Rotation(int value) {
            this.value = value;
        }
    }
}
