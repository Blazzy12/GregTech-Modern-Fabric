package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.client.event.TextureAtlasStitchedEvent;

import org.jetbrains.annotations.Nullable;

public interface AssetEventListener<T extends Event> {

    void accept(T event);

    default @Nullable Class<T> eventClass() {
        return null;
    }

    @FunctionalInterface
    interface AtlasStitched extends AssetEventListener<TextureAtlasStitchedEvent> {

        @Override
        @Nullable
        default Class<TextureAtlasStitchedEvent> eventClass() {
            return TextureAtlasStitchedEvent.class;
        }
    }
}
