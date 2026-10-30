package com.gregtechceu.gtceu.compat.common;

import net.fabricmc.loader.api.FabricLoader;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A small replacement for NeoForge's config spec, enough for ModularUI's client config. Values are stored in a
 * flat {@code key = value} file and saved whenever a value is set.
 */
public final class ModConfigSpec {

    private final Map<List<String>, ConfigValue<?>> values;
    private @Nullable Path file;

    private ModConfigSpec(Map<List<String>, ConfigValue<?>> values) {
        this.values = values;
        values.values().forEach(v -> v.spec = this);
    }

    public Map<List<String>, ConfigValue<?>> getValues() {
        return values;
    }

    /** @return whether the config file was loaded by {@link #register(String)} */
    public boolean isLoaded() {
        return file != null;
    }

    /** Loads the file from the config directory, creating it with defaults when missing. */
    public void register(String fileName) {
        this.file = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        if (Files.exists(file)) {
            try {
                for (String line : Files.readAllLines(file)) {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq < 0) continue;
                    var key = List.of(line.substring(0, eq).strip().split("\\."));
                    ConfigValue<?> value = values.get(key);
                    if (value != null) value.parse(line.substring(eq + 1).strip());
                }
            } catch (IOException ignored) {}
        }
        save();
    }

    void save() {
        if (file == null) return;
        StringBuilder out = new StringBuilder();
        for (var entry : values.entrySet()) {
            for (String comment : entry.getValue().comments) out.append("# ").append(comment).append('\n');
            out.append(String.join(".", entry.getKey())).append(" = ").append(entry.getValue().get()).append("\n\n");
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, out.toString());
        } catch (IOException ignored) {}
    }

    public static class Builder {

        private final Deque<String> path = new ArrayDeque<>();
        private final Map<List<String>, ConfigValue<?>> values = new LinkedHashMap<>();
        private final List<String> comments = new ArrayList<>();

        public Builder push(String name) {
            path.addLast(name);
            return this;
        }

        public Builder pop() {
            path.removeLast();
            return this;
        }

        public Builder comment(String... lines) {
            comments.addAll(Arrays.asList(lines));
            return this;
        }

        public Builder translation(String key) {
            return this;
        }

        public Builder gameRestart() {
            return this;
        }

        public Builder worldRestart() {
            return this;
        }

        private <T, V extends ConfigValue<T>> V add(String name, V value) {
            List<String> key = new ArrayList<>(path);
            key.add(name);
            value.comments = List.copyOf(comments);
            comments.clear();
            values.put(List.copyOf(key), value);
            return value;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return add(name, new BooleanValue(defaultValue));
        }

        public <T> ConfigValue<T> define(String name, T defaultValue) {
            return add(name, new ConfigValue<>(defaultValue, s -> parseLike(defaultValue, s), v -> true));
        }

        public <T> ConfigValue<T> define(String name, T defaultValue, Predicate<Object> validator) {
            return add(name, new ConfigValue<>(defaultValue, s -> parseLike(defaultValue, s), validator));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return add(name, new IntValue(defaultValue, min, max));
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return add(name, new DoubleValue(defaultValue, min, max));
        }

        public <E extends Enum<E>> EnumValue<E> defineEnum(String name, E defaultValue) {
            return add(name, new EnumValue<>(defaultValue));
        }

        public ModConfigSpec build() {
            return new ModConfigSpec(values);
        }

        @SuppressWarnings("unchecked")
        private static <T> T parseLike(T defaultValue, String s) {
            return (T) s;
        }
    }

    public static class ConfigValue<T> {

        private final T defaultValue;
        private final Function<String, T> parser;
        private final Predicate<Object> validator;
        private T value;
        List<String> comments = List.of();
        @Nullable
        ModConfigSpec spec;

        ConfigValue(T defaultValue, Function<String, T> parser, Predicate<Object> validator) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.parser = parser;
            this.validator = validator;
        }

        public T get() {
            return value;
        }

        public T getAsDefault() {
            return defaultValue;
        }

        public T getDefault() {
            return defaultValue;
        }

        public void set(T value) {
            if (validator.test(value)) this.value = value;
            if (spec != null) spec.save();
        }

        public void save() {
            if (spec != null) spec.save();
        }

        void parse(String text) {
            try {
                T parsed = parser.apply(text);
                if (parsed != null && validator.test(parsed)) value = parsed;
            } catch (RuntimeException ignored) {}
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {

        BooleanValue(boolean defaultValue) {
            super(defaultValue, Boolean::parseBoolean, v -> true);
        }

        public boolean getAsBoolean() {
            return get();
        }
    }

    public static class IntValue extends ConfigValue<Integer> {

        IntValue(int defaultValue, int min, int max) {
            super(defaultValue, Integer::parseInt, v -> v instanceof Integer i && i >= min && i <= max);
        }

        public int getAsInt() {
            return get();
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {

        DoubleValue(double defaultValue, double min, double max) {
            super(defaultValue, Double::parseDouble, v -> v instanceof Double d && d >= min && d <= max);
        }

        public double getAsDouble() {
            return get();
        }
    }

    public static class EnumValue<E extends Enum<E>> extends ConfigValue<E> {

        EnumValue(E defaultValue) {
            super(defaultValue, s -> Enum.valueOf(defaultValue.getDeclaringClass(), s), v -> true);
        }
    }
}
