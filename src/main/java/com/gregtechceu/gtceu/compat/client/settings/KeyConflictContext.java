package com.gregtechceu.gtceu.compat.client.settings;

import net.minecraft.client.Minecraft;

public enum KeyConflictContext implements IKeyConflictContext {

    UNIVERSAL {

        @Override
        public boolean isActive() {
            return true;
        }
    },
    GUI {

        @Override
        public boolean isActive() {
            return Minecraft.getInstance().gui.screen() != null;
        }
    },
    IN_GAME {

        @Override
        public boolean isActive() {
            return Minecraft.getInstance().gui.screen() == null;
        }
    };

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this == UNIVERSAL || other == UNIVERSAL || this == other;
    }
}
