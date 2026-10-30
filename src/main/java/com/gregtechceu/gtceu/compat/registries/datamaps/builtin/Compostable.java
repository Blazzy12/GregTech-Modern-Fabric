package com.gregtechceu.gtceu.compat.registries.datamaps.builtin;

/** Composting chance, mapped to the nearest vanilla compostable tier. */
public record Compostable(float chance, boolean canVillagerCompost) {

    public Compostable(float chance) {
        this(chance, false);
    }
}
