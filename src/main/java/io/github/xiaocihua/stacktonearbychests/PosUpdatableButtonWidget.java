package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PosUpdatableButtonWidget extends TexturedButtonWidget {
    private final HandledScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Integer>> xUpdater;
    private final Optional<Function<HandledScreenAccessor, Integer>> yUpdater;

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
                                     TooltipSupplier tooltipSupplier,
                                     Text text,
                                     HandledScreen<?> parent,
                                     Optional<Function<HandledScreenAccessor, Integer>> xUpdater,
                                     Optional<Function<HandledScreenAccessor, Integer>> yUpdater) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, tooltipSupplier, text);
        this.parent = parent;
        this.xUpdater = xUpdater;
        this.yUpdater = yUpdater;
        Screens.getButtons(parent).add(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        xUpdater.ifPresent(updater -> x = updater.apply((HandledScreenAccessor) parent));
        yUpdater.ifPresent(updater -> y = updater.apply((HandledScreenAccessor) parent));
        super.renderButton(matrices, mouseX, mouseY, delta);
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
        private TooltipSupplier tooltipSupplier = (button, matrices, mouseX, mouseY) -> {};
        private Text text = ScreenTexts.EMPTY;
        private HandledScreen<?> parent = null;
        private Optional<Function<HandledScreenAccessor, Integer>> xUpdater = Optional.empty();
        private Optional<Function<HandledScreenAccessor, Integer>> yUpdater = Optional.empty();

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

        public Builder setTooltipSupplier(TooltipSupplier tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public Builder setText(Text text) {
            this.text = text;
            return this;
        }

        public Builder setXUpdater(Function<HandledScreenAccessor, Integer> xUpdater) {
            this.xUpdater = Optional.ofNullable(xUpdater);
            return this;
        }

        public Builder setYUpdater(Function<HandledScreenAccessor, Integer> yUpdater) {
            this.yUpdater = Optional.ofNullable(yUpdater);
            return this;
        }

        public PosUpdatableButtonWidget build() {
            return new PosUpdatableButtonWidget(x,
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
                    tooltipSupplier,
                    text,
                    parent,
                    xUpdater,
                    yUpdater);
        }
    }
}
