package com.gregtechceu.gtceu.compat.fml.loading;

import com.gregtechceu.gtceu.compat.dist.Dist;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class FMLEnvironment {

    public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ?
            Dist.CLIENT : Dist.DEDICATED_SERVER;

    public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();

    private FMLEnvironment() {}
}
