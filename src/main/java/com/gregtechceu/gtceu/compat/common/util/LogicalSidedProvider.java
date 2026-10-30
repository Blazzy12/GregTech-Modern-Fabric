package com.gregtechceu.gtceu.compat.common.util;

import com.gregtechceu.gtceu.compat.fml.LogicalSide;

import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Function;

/** Side-dependent lookups. Only the client level is needed by GT. */
public final class LogicalSidedProvider<T> {

    public static final LogicalSidedProvider<Optional<Level>> CLIENTWORLD = new LogicalSidedProvider<>(
            side -> side.isClient() ? Optional.ofNullable(ClientAccess.level()) : Optional.empty());

    private final Function<LogicalSide, T> getter;

    private LogicalSidedProvider(Function<LogicalSide, T> getter) {
        this.getter = getter;
    }

    public T get(LogicalSide side) {
        return getter.apply(side);
    }

    private static final class ClientAccess {

        static Level level() {
            return net.minecraft.client.Minecraft.getInstance().level;
        }
    }
}
