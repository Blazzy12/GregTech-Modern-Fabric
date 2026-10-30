package com.gregtechceu.gtceu.compat.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** An action a tool can perform, such as stripping logs. */
public final class ItemAbility {

    private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

    private final String name;

    private ItemAbility(String name) {
        this.name = name;
    }

    public static ItemAbility get(String name) {
        return ABILITIES.computeIfAbsent(name, ItemAbility::new);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ItemAbility[" + name + "]";
    }
}
