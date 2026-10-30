package com.gregtechceu.gtceu.compat;

import java.lang.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/** Replacement for the vanilla nullness default removed in 26.x. */
@Nonnull
@TypeQualifierDefault(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MethodsReturnNonnullByDefault {}
