package com.gregtechceu.gtceu.compat.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Accessors that were removed from {@link ChatFormatting} in 26.x. */
public final class ChatFormats {

    private ChatFormats() {}

    /** @return the RGB colour of a colour formatting, or {@code null} for style codes */
    public static @Nullable Integer getColor(ChatFormatting format) {
        TextColor color = TextColor.fromLegacyFormat(format);
        return color == null ? null : color.getValue();
    }

    public static String getName(ChatFormatting format) {
        return format.name().toLowerCase(Locale.ROOT);
    }

    public static char getChar(ChatFormatting format) {
        return format.toString().charAt(1);
    }

    public static @Nullable ChatFormatting getByName(@Nullable String name) {
        if (name == null) return null;
        try {
            return ChatFormatting.valueOf(name.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isColor(ChatFormatting format) {
        return TextColor.fromLegacyFormat(format) != null;
    }
}
