package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.resources.Identifier;

/**
 * One texture layer of a worn armor piece. Replaces the 1.21.1 {@code com.gregtechceu.gtceu.api.item.armor.ArmorLayer}; GT renders its armor
 * through a Fabric armor renderer that resolves these to textures.
 */
public record ArmorLayer(Identifier assetName, String suffix, boolean dyeable) {

    public ArmorLayer(Identifier assetName) {
        this(assetName, "", false);
    }

    /** The worn texture for this layer, {@code textures/models/armor/<name>_layer_<1|2><suffix>.png}. */
    public Identifier texture(boolean innerModel) {
        return assetName.withPath(path -> "textures/models/armor/" + path + "_layer_" + (innerModel ? 2 : 1) +
                suffix + ".png");
    }
}
