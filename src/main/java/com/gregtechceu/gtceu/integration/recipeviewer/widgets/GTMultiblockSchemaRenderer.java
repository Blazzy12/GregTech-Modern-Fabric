package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.ISchema;

/**
 * The schema renderer of multiblock previews. GT's emissive overlays are ordinary cutout quads on 26.x, so this no
 * longer needs extra render passes.
 */
public class GTMultiblockSchemaRenderer extends SchemaRenderer {

    public GTMultiblockSchemaRenderer(ISchema schema) {
        super(schema);
    }
}
