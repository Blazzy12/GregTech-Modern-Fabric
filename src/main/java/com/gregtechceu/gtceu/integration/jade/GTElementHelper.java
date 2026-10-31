package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import net.minecraft.network.chat.Component;

import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.util.FluidTextHelper;

public class GTElementHelper {

    public static Element smallFluid(JadeFluidObject fluid) {
        return JadeUI.fluid(fluid).size(10, 10).offset(0, -1);
    }

    /** A progress bar of one colour, in Jade's box style for nested ({@code nested}) or top-level views. */
    public static Element progress(float ratio, Component text, int color, boolean nested) {
        return JadeUI.progress(new ProgressView(ProgressView.Part.of(ratio, color), text, JadeUI.progressStyle(),
                nested ? BoxStyle.DEFAULT_NESTED_BOX : BoxStyle.DEFAULT_VIEW_GROUP));
    }

    /** A tank of {@code capacity} millibuckets holding {@code stack}, as Jade's fluid view data. */
    public static FluidView.Data fluidData(FluidStack stack, long capacity) {
        return new FluidView.Data(fluid(stack), capacity * JadeFluidObject.bucketVolume() / 1000);
    }

    /** A tank of {@code capacity} millibuckets holding {@code amount} millibuckets of {@code type}'s fluid. */
    public static FluidView.Data fluidData(FluidStack type, long amount, long capacity) {
        return new FluidView.Data(JadeFluidObject.of(type.getFluid(), amount * JadeFluidObject.bucketVolume() / 1000,
                type.getComponentsPatch()), capacity * JadeFluidObject.bucketVolume() / 1000);
    }

    /** A millibucket amount, formatted by Jade (which counts in its platform's units). */
    public static Component millibuckets(long amount) {
        return FluidTextHelper.getMillibuckets(amount * JadeFluidObject.bucketVolume() / 1000, true);
    }

    /** GT's millibucket fluid stacks as Jade fluids, which count in their platform's units. */
    public static JadeFluidObject fluid(FluidStack stack) {
        return JadeFluidObject.of(stack.getFluid(), stack.getAmount() * JadeFluidObject.bucketVolume() / 1000,
                stack.getComponentsPatch());
    }
}
