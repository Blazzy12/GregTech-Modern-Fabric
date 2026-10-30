package com.gregtechceu.gtceu.integration.jade;

import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.Element;

/**
 * Adds Jade elements to a tooltip. On Fabric, ITooltip takes the client-only LayoutElement, and passing an Element
 * to it directly makes the verifier load that class, so a provider calling it could not load on a dedicated server.
 * Only the client uses this class.
 */
public final class JadeTooltips {

    private JadeTooltips() {}

    public static void add(ITooltip tooltip, Element element) {
        tooltip.add(element);
    }

    public static void append(ITooltip tooltip, Element element) {
        tooltip.append(element);
    }
}
