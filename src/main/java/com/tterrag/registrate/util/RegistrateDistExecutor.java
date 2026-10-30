package com.tterrag.registrate.util;

import com.gregtechceu.gtceu.compat.dist.Dist;
import com.gregtechceu.gtceu.compat.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public class RegistrateDistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == FMLEnvironment.dist) {
            toRun.get().run();
        }
    }
}
