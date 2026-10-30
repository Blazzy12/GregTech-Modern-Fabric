package com.gregtechceu.gtceu.compat.client.settings;

/** When a key mapping is active. Vanilla has no conflict contexts, so this only documents intent. */
public interface IKeyConflictContext {

    boolean isActive();

    boolean conflicts(IKeyConflictContext other);
}
