package com.gregtechceu.gtceu.compat.client.model.data;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable per-block data passed from a block entity to its model. On Fabric it is delivered as the block
 * entity's render data ({@code RenderDataBlockEntity}).
 */
public final class ModelData {

    public static final ModelData EMPTY = new ModelData(Map.of());

    private final Map<ModelProperty<?>, Object> properties;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public Set<ModelProperty<?>> getProperties() {
        return properties.keySet();
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static <T> ModelData of(ModelProperty<T> property, T value) {
        return builder().with(property, value).build();
    }

    public static final class Builder {

        private final Map<ModelProperty<?>, Object> properties = new IdentityHashMap<>();

        private Builder(@Nullable ModelData parent) {
            if (parent != null) properties.putAll(parent.properties);
        }

        public <T> Builder with(ModelProperty<T> property, T value) {
            if (!property.test(value)) throw new IllegalArgumentException("Invalid value for property");
            properties.put(property, value);
            return this;
        }

        public ModelData build() {
            return properties.isEmpty() ? EMPTY : new ModelData(Map.copyOf(properties));
        }
    }
}
