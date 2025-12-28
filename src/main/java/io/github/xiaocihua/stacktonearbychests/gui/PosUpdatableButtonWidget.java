package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PosUpdatableButtonWidget extends TexturedButtonWidget {
    private final HandledScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableButtonWidget(int width,
                                     int height,
                                     ButtonTextures textures,
                                     PressAction pressAction,
                                     net.minecraft.text.Text text,
                                     HandledScreen<?> parent,
                                     Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater) {
        super(0, 0, width, height, textures, pressAction, text);
        this.parent = parent;
        this.posUpdater = posUpdater;
        Screens.getButtons(parent).add(this);
    }

    @Override
    public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        posUpdater.ifPresent(updater -> setPos(updater.apply((HandledScreenAccessor) parent)));
        super.drawIcon(context, mouseX, mouseY, delta);
//        Identifier identifier = this.textures.get(this.isNarratable(), this.isHovered());
//        context.drawGuiTexture(identifier, this.getX(), this.getY(), this.width, this.height);
    }

    public void setPos(Vec2i pos) {
        setX(pos.x());
        setY(pos.y());
    }

    public static class Builder {
        private int width = 16;
        private int height = 16;
        private ButtonTextures textures;
        private PressAction pressAction = button -> {};
        @Nullable
        private Tooltip tooltip;
        private net.minecraft.text.Text text = ScreenTexts.EMPTY;
        private final HandledScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(HandledScreen<?> parent) {
            this.parent = parent;
        }

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setTextures(ButtonTextures textures) {
            this.textures = textures;
            return this;
        }

        public Builder setPressAction(PressAction pressAction) {
            this.pressAction = pressAction;
            return this;
        }

        public Builder setTooltip(@Nullable net.minecraft.text.Text content) {
            if (content != null) {
                this.tooltip = Tooltip.of(content);
            }
            return this;
        }

        public Builder setTooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder setText(net.minecraft.text.Text text) {
            this.text = text;
            return this;
        }

        public Builder setPosUpdater(Function<HandledScreenAccessor, Vec2i> posUpdater) {
            this.posUpdater = Optional.ofNullable(posUpdater);
            return this;
        }

        public PosUpdatableButtonWidget build() {
            PosUpdatableButtonWidget button =
                    new PosUpdatableButtonWidget(width, height, textures, pressAction, text, parent, posUpdater);
            button.setTooltip(tooltip);
            return button;
        }
    }
}
