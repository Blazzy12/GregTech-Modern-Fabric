package brachy.modularui.integration.recipeviewer.entry.fluid;

import brachy.modularui.integration.recipeviewer.entry.EntryList;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import java.util.List;

public sealed interface FluidEntryList extends EntryList<FluidStack> permits FluidStackList, FluidTagList, FluidHolderSetList {

    List<FluidStack> getStacks();

    boolean isEmpty();

    @Override
    default Class<FluidStack> getType() {
        return FluidStack.class;
    }
}
