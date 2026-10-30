package com.gregtechceu.gtceu.compat.fml.common;

import com.gregtechceu.gtceu.compat.dist.Dist;

import java.lang.annotation.*;

/**
 * Marker kept from NeoForge. Fabric has no annotation scanning, so annotated classes are registered
 * explicitly by {@link com.gregtechceu.gtceu.compat.fml.EventSubscribers}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {

    Dist[] value() default { Dist.CLIENT, Dist.DEDICATED_SERVER };

    String modid() default "";

    Bus bus() default Bus.GAME;

    enum Bus {
        GAME,
        MOD
    }
}
