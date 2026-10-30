package com.tterrag.registrate.providers;

import com.gregtechceu.gtceu.compat.data.event.GatherDataEvent;
import com.gregtechceu.gtceu.compat.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;

/** Datagen-only provider. Datagen does not run on Fabric. */
public class RegistrateGenericProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> owner;
    private final LogicalSide side;

    public RegistrateGenericProvider(AbstractRegistrate<?> owner, GatherDataEvent event, LogicalSide side,
                                     ProviderType<RegistrateGenericProvider> type) {
        this.owner = owner;
        this.side = side;
    }

    @Override
    public LogicalSide getSide() {
        return side;
    }

    @Override
    public String getName() {
        return "Generic " + side + " (" + owner.getModid() + ")";
    }
}
