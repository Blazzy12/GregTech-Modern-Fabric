package com.gregtechceu.gtceu.compat.common.util;

import java.util.function.Supplier;

/** A thread-safe memoizing supplier. */
public interface Lazy<T> extends Supplier<T> {

    static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>() {

            private volatile boolean done;
            private T value;

            @Override
            public T get() {
                if (!done) {
                    synchronized (this) {
                        if (!done) {
                            value = supplier.get();
                            done = true;
                        }
                    }
                }
                return value;
            }
        };
    }
}
