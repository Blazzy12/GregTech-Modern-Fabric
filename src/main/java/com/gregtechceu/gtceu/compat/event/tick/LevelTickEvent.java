package com.gregtechceu.gtceu.compat.event.tick;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.level.Level;

public abstract class LevelTickEvent extends Event {

    private final Level level;

    public LevelTickEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public static class Pre extends LevelTickEvent {

        public Pre(Level level) {
            super(level);
        }
    }

    public static class Post extends LevelTickEvent {

        public Post(Level level) {
            super(level);
        }
    }
}
