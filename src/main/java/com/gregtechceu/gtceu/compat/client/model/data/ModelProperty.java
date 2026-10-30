package com.gregtechceu.gtceu.compat.client.model.data;

import java.util.function.Predicate;

/** A typed key into {@link ModelData}. */
public class ModelProperty<T> implements Predicate<T> {

    private final Predicate<T> predicate;

    public ModelProperty() {
        this(t -> true);
    }

    public ModelProperty(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(T value) {
        return predicate.test(value);
    }
}
