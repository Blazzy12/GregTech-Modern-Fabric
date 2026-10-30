package com.gregtechceu.gtceu.compat.energy;

/** Forge Energy style storage, used for FE interop. */
public interface IEnergyStorage {

    int receiveEnergy(int toReceive, boolean simulate);

    int extractEnergy(int toExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
