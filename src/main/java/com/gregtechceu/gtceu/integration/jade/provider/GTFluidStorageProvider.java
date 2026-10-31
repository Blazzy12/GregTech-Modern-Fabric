package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.FluidStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.util.FluidTextHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Custom FluidView info provider for any machines that require it
 * Currently: Quantum Tanks, locked Fluid Hatches
 * Defaults to Jade's normal FluidView provider
 */
public enum GTFluidStorageProvider implements IServerExtensionProvider<FluidView.Data>,
        IClientExtensionProvider<FluidView.Data, FluidView> {

    INSTANCE;

    /** Marks a group showing a locked fluid of an empty tank, which Jade's views cannot show as empty. */
    private static final String LOCKED = "gtceu_locked";

    @Override
    public Identifier getUid() {
        return GTCEu.id("custom_fluid_storage");
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor,
                                                            List<ViewGroup<FluidView.Data>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {
            if (group.getExtraData().getBooleanOr(LOCKED, false)) {
                for (FluidView view : clientGroup.views) {
                    view.current = FluidTextHelper.getMillibuckets(0, true);
                    view.ratio = 0;
                }
            }
        });
    }

    @Override
    public @Nullable List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
        if (accessor.getTarget() instanceof QuantumTankMachine qtm) {
            FluidStack stored = qtm.getStored();
            if (stored.isEmpty() && qtm instanceof CreativeTankMachine) return Collections.emptyList();
            boolean locked = stored.isEmpty() && qtm.isLocked();
            if (locked) stored = qtm.getLockedFluid();
            if (stored.isEmpty()) return Collections.emptyList();
            // the stored stack only marks the fluid; the amount is a separate long, as tanks can hold more than an int
            return List.of(group(GTElementHelper.fluidData(stored, locked ? 1000 : qtm.getStoredAmount(),
                    qtm.getMaxAmount()), locked));
        } else if (accessor.getTarget() instanceof FluidHatchPartMachine hatch) {
            if (hatch.tank.getTanks() == 1 && hatch.tank.getFluidInTank(0).isEmpty() && hatch.tank.isLocked()) {
                FluidStack stored = hatch.tank.getLockedFluid().getFluid();
                return List.of(group(GTElementHelper.fluidData(stored.copyWithAmount(1000),
                        hatch.tank.getTankCapacity(0)), true));
            }
        }

        return FluidStorageProvider.Extension.INSTANCE.getGroups(accessor);
    }

    private static ViewGroup<FluidView.Data> group(FluidView.Data data, boolean locked) {
        CompoundTag extra = new CompoundTag();
        if (locked) extra.putBoolean(LOCKED, true);
        return new ViewGroup<>(List.of(data), Optional.empty(), Optional.of(extra));
    }
}
