package brachy.modularui.animation;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AnimatorManager {

    private static final List<IAnimator> animators = new ArrayList<>(16);
    private static final List<IAnimator> queuedAnimators = new ArrayList<>(8);
    private static long lastTime = 0;
    private static boolean waitClearAnimators = false;

    static void startAnimation(IAnimator animator) {
        if (!animators.contains(animator) && !queuedAnimators.contains(animator)) {
            queuedAnimators.add(animator);
        }
    }

    private AnimatorManager() {}

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenEvents.beforeExtract(screen).register((s, graphics, mouseX, mouseY, partialTick) -> onDraw());
            // stop and yeet all animators on gui close
            // we can't clear now otherwise we might get a CME because of multithreading
            ScreenEvents.remove(screen).register(s -> waitClearAnimators = true);
        });
    }

    private static void onDraw() {
        long time = Util.getMillis();
        int elapsedTime = IAnimator.getTimeDiff(lastTime, time);
        checkClearAnimators();
        if (lastTime > 0 && !animators.isEmpty()) {
            animators.removeIf(animator -> {
                if (animator == null) return true;
                if (animator.isPaused()) return false;
                animator.advance(elapsedTime);
                return !animator.isAnimating();
            });
        }
        lastTime = time;
        animators.addAll(queuedAnimators);
        queuedAnimators.clear();
        checkClearAnimators();
    }

    private static void checkClearAnimators() {
        if (waitClearAnimators) {
            waitClearAnimators = false;
            animators.forEach(iAnimator -> iAnimator.stop(false));
            animators.clear();
        }
    }
}
