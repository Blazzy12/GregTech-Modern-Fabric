package com.gregtechceu.gtceu.compat.fml.util;

import java.lang.reflect.Field;

/** 26.x is unobfuscated, so fields are looked up by their real names. */
public final class ObfuscationReflectionHelper {

    private ObfuscationReflectionHelper() {}

    @SuppressWarnings("unchecked")
    public static <T, E> T getPrivateValue(Class<? super E> clazz, E instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to read " + clazz.getName() + "." + fieldName, e);
        }
    }

    public static <T, E> void setPrivateValue(Class<? super T> clazz, T instance, E value, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to write " + clazz.getName() + "." + fieldName, e);
        }
    }
}
