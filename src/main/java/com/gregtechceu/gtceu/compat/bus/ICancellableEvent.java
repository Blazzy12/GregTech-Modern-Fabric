package com.gregtechceu.gtceu.compat.bus;

public interface ICancellableEvent {

    default void setCanceled(boolean canceled) {
        ((Event) this).canceled = canceled;
    }

    default boolean isCanceled() {
        return ((Event) this).canceled;
    }
}
