package com.gregtechceu.gtceu.compat.client.gui;

/** Text colours the way fonts read them before 26.x. */
public final class TextColors {

    private TextColors() {}

    /**
     * Pre-26 fonts drew colours with an alpha below 4 as opaque, so plain RGB colours worked; 26.x skips text with
     * zero alpha.
     */
    public static int legacy(int color) {
        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }
}
