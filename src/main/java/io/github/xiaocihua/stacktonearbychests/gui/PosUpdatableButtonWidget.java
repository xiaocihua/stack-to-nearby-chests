package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PosUpdatableButtonWidget extends TexturedButtonWidget {
    private final HandledScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableButtonWidget(int x,
                                     int y,
                                     int width,
                                     int height,
                                     int u,
                                     int v,
                                     int hoveredVOffset,
                                     Identifier texture,
                                     int textureWidth,
                                     int textureHeight,
                                     PressAction pressAction,
                                     Text text,
                                     HandledScreen<?> parent,
                                     Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, text);
        this.parent = parent;
        this.posUpdater = posUpdater;
        Screens.getButtons(parent).add(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        posUpdater.ifPresent(updater -> setPos(updater.apply((HandledScreenAccessor) parent)));
        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    public void setPos(Vec2i pos) {
        super.setPos(pos.x(), pos.y());
    }

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width = 16;
        private int height = 16;
        private int u = 0;
        private int v = 0;
        private int hoveredVOffset = 16;
        private Identifier texture = MissingSprite.getMissingSpriteId();
        private int textureWidth = 16;
        private int textureHeight = 16;
        private PressAction pressAction = button -> {};
        @Nullable
        private Tooltip tooltip;
        private Text text = ScreenTexts.EMPTY;
        private HandledScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(HandledScreen<?> parent) {
            this.parent = parent;
        }

        public Builder setPos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setUV(int u, int v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public Builder setHoveredVOffset(int hoveredVOffset) {
            this.hoveredVOffset = hoveredVOffset;
            return this;
        }

        public Builder setTexture(Identifier texture, int textureWidth, int textureHeight) {
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            return this;
        }

        public Builder setPressAction(PressAction pressAction) {
            this.pressAction = pressAction;
            return this;
        }

        public Builder setTooltip(Text content) {
            this.tooltip = Tooltip.of(content);
            return this;
        }

        public Builder setTooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder setText(Text text) {
            this.text = text;
            return this;
        }

        public Builder setPosUpdater(Function<HandledScreenAccessor, Vec2i> posUpdater) {
            this.posUpdater = Optional.ofNullable(posUpdater);
            return this;
        }

        public PosUpdatableButtonWidget build() {
            PosUpdatableButtonWidget button = new PosUpdatableButtonWidget(x,
                    y,
                    width,
                    height,
                    u,
                    v,
                    hoveredVOffset,
                    texture,
                    textureWidth,
                    textureHeight,
                    pressAction,
                    text,
                    parent,
                    posUpdater);
            button.setTooltip(tooltip);
            return button;
        }
    }
}
