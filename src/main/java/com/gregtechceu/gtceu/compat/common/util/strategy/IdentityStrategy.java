package com.gregtechceu.gtceu.compat.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;

public final class IdentityStrategy implements Hash.Strategy<Object> {

    @SuppressWarnings("rawtypes")
    public static final Hash.Strategy IDENTITY = new IdentityStrategy();

    @Override
    public int hashCode(Object o) {
        return System.identityHashCode(o);
    }

    @Override
    public boolean equals(Object a, Object b) {
        return a == b;
    }
}
