package com.gregtechceu.gtceu.compat.event.level;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.level.LevelAccessor;

public abstract class LevelEvent extends Event {

    private final LevelAccessor level;

    public LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public static class Load extends LevelEvent {

        public Load(LevelAccessor level) {
            super(level);
        }
    }

    public static class Unload extends LevelEvent {

        public Unload(LevelAccessor level) {
            super(level);
        }
    }

    public static class Save extends LevelEvent {

        public Save(LevelAccessor level) {
            super(level);
        }
    }
}
