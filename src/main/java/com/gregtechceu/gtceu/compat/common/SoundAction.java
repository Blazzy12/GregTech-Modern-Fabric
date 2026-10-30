package com.gregtechceu.gtceu.compat.common;

public record SoundAction(String name) {

    public static SoundAction get(String name) {
        return new SoundAction(name);
    }
}
