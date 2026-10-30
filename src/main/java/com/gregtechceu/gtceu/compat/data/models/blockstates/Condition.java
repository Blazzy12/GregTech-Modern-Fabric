package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.world.level.block.state.properties.Property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** A multipart {@code "when"} condition. */
public interface Condition extends Supplier<JsonElement> {

    static TerminalCondition condition() {
        return new TerminalCondition();
    }

    static Condition or(Condition... conditions) {
        return () -> {
            JsonArray array = new JsonArray();
            for (Condition c : conditions) array.add(c.get());
            JsonObject json = new JsonObject();
            json.add("OR", array);
            return json;
        };
    }

    static Condition and(Condition... conditions) {
        return () -> {
            JsonArray array = new JsonArray();
            for (Condition c : conditions) array.add(c.get());
            JsonObject json = new JsonObject();
            json.add("AND", array);
            return json;
        };
    }

    final class TerminalCondition implements Condition {

        private final Map<String, String> terms = new LinkedHashMap<>();

        @SafeVarargs
        public final <T extends Comparable<T>> TerminalCondition term(Property<T> property, T value, T... others) {
            List<T> all = new ArrayList<>();
            all.add(value);
            all.addAll(Arrays.asList(others));
            terms.put(property.getName(), all.stream().map(property::getName).collect(Collectors.joining("|")));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> TerminalCondition negatedTerm(Property<T> property, T value,
                                                                             T... others) {
            term(property, value, others);
            terms.computeIfPresent(property.getName(), (k, v) -> "!" + v);
            return this;
        }

        @Override
        public JsonElement get() {
            JsonObject json = new JsonObject();
            terms.forEach(json::addProperty);
            return json;
        }
    }
}
