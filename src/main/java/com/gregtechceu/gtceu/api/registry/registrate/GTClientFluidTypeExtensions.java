package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public static final Identifier FLUID_SCREEN_OVERLAY = GTCEu.id("textures/misc/fluid_screen_overlay.png");

    public GTClientFluidTypeExtensions(@Nullable Identifier stillTexture,
                                       @Nullable Identifier flowingTexture,
                                       int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    /**
     * Textures worked out by {@code textureResolver} when first asked for. Material fluid textures depend on which
     * icon set textures exist, which can only be checked once resources have loaded (on Fabric, client extensions are
     * registered before that), so the resolver runs again until it gives something other than the blank texture.
     */
    public GTClientFluidTypeExtensions(TextureResolver textureResolver, int tintColor) {
        this(null, null, tintColor);
        this.textureResolver = textureResolver;
    }

    @Setter
    @Nullable
    private Identifier flowingTexture, stillTexture;
    @Getter
    @Setter
    private int tintColor;
    @Nullable
    private TextureResolver textureResolver;

    @Override
    public @Nullable Identifier getStillTexture() {
        resolveTextures();
        return stillTexture;
    }

    @Override
    public @Nullable Identifier getFlowingTexture() {
        resolveTextures();
        return flowingTexture;
    }

    private void resolveTextures() {
        if (textureResolver == null) return;
        if (stillTexture != null && !stillTexture.equals(GTModels.BLANK_TEXTURE)) return;
        Identifier[] textures = textureResolver.resolve();
        stillTexture = textures[0];
        flowingTexture = textures[1];
    }

    @Override
    public Identifier getRenderOverlayTexture(Minecraft mc) {
        return FLUID_SCREEN_OVERLAY;
    }

    @FunctionalInterface
    public interface TextureResolver {

        /** @return the still and flowing textures */
        Identifier[] resolve();
    }
}
