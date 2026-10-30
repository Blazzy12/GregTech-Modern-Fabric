package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RegisterCommandsEvent extends Event {

    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final Commands.CommandSelection commandSelection;
    private final CommandBuildContext buildContext;

    public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection commandSelection, CommandBuildContext buildContext) {
        this.dispatcher = dispatcher;
        this.commandSelection = commandSelection;
        this.buildContext = buildContext;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public Commands.CommandSelection getCommandSelection() {
        return commandSelection;
    }

    public CommandBuildContext getBuildContext() {
        return buildContext;
    }
}
