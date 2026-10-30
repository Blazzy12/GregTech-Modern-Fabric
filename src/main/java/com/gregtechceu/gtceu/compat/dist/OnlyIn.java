package com.gregtechceu.gtceu.compat.dist;

import java.lang.annotation.*;

/**
 * Documentation-only side marker. Fabric does not strip members, so this has no runtime effect.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
public @interface OnlyIn {

    Dist value();
}
