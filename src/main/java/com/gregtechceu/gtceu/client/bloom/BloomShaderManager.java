package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.config.ConfigHolder;

/**
 * Whether bloom effects are drawn.
 * <p>
 * The bloom post effect (a blurred, separate render target) has not been ported to 26.x's render graph yet; bloom
 * effects are drawn full-bright into the world instead, without the blur.
 */
public final class BloomShaderManager {

    private BloomShaderManager() {}

    public static boolean isBloomActive() {
        return ConfigHolder.INSTANCE.client.bloom.type != BloomType.DISABLED;
    }
}
