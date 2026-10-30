package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Maps property values to variants, keyed by selector strings such as {@code facing=north,up=true}. */
public abstract class PropertyDispatch {

    protected final Map<String, List<Variant>> values = new LinkedHashMap<>();

    public Map<String, List<Variant>> getEntries() {
        return values;
    }

    public abstract List<Property<?>> getDefinedProperties();

    protected void put(List<String> parts, List<Variant> variants) {
        values.put(String.join(",", parts), variants);
    }

    protected static <T extends Comparable<T>> String term(Property<T> property, T value) {
        return property.getName() + "=" + property.getName(value);
    }

    public static <T extends Comparable<T>> C1<T> property(Property<T> property) {
        return new C1<>(property);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> C2<T1, T2> properties(Property<T1> p1,
                                                                                            Property<T2> p2) {
        return new C2<>(p1, p2);
    }

    public static final class C1<T extends Comparable<T>> extends PropertyDispatch {

        private final Property<T> property;

        private C1(Property<T> property) {
            this.property = property;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(property);
        }

        public C1<T> select(T value, Variant variant) {
            put(List.of(term(property, value)), List.of(variant));
            return this;
        }

        public C1<T> select(T value, List<Variant> variants) {
            put(List.of(term(property, value)), variants);
            return this;
        }

        public PropertyDispatch generate(Function<T, Variant> generator) {
            property.getPossibleValues().forEach(v -> select(v, generator.apply(v)));
            return this;
        }

        public PropertyDispatch generateList(Function<T, List<Variant>> generator) {
            property.getPossibleValues().forEach(v -> select(v, generator.apply(v)));
            return this;
        }
    }

    public static final class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {

        private final Property<T1> p1;
        private final Property<T2> p2;

        private C2(Property<T1> p1, Property<T2> p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(p1, p2);
        }

        public C2<T1, T2> select(T1 v1, T2 v2, Variant variant) {
            put(List.of(term(p1, v1), term(p2, v2)), List.of(variant));
            return this;
        }

        public PropertyDispatch generate(BiFunction<T1, T2, Variant> generator) {
            for (T1 v1 : p1.getPossibleValues()) {
                for (T2 v2 : p2.getPossibleValues()) select(v1, v2, generator.apply(v1, v2));
            }
            return this;
        }
    }
}
