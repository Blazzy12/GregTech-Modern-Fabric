package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;


public class EmbedHandler {

    public static int getEmbedWidth(ModularScreen screen) {
        return screen.getMainPanel().getArea().width;
    }

    public static int getEmbedHeight(ModularScreen screen) {
        return screen.getMainPanel().getArea().height;
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        drawEmbed(screen, graphics, mouseX, mouseY, partialTicks, null, null);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks,
                                 @Nullable Runnable beforeDraw, @Nullable Runnable afterDraw) {
        screen.getContext().reset();
        graphics.pose().pushMatrix();

        if (beforeDraw != null) beforeDraw.run();

        int mx = screen.getContext().unTransformX(mouseX, mouseY);
        int my = screen.getContext().unTransformY(mouseX, mouseY);
        screen.extractRenderState(graphics, mx, my, partialTicks);

        screen.drawForeground(graphics);

        if (afterDraw != null) afterDraw.run();

        brachy.modularui.drawable.GuiDraw.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        graphics.pose().popMatrix();
    }

    public record EmbedWrapper(ModularScreen screen) implements IMuiScreen {

        @Override
        public Screen wrappedScreen() {
            return Minecraft.getInstance().gui.screen();
        }

        @Override
        public void updateGuiArea(Rectangle area) {}
    }
}
