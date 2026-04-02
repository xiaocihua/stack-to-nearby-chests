package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PosUpdatableButtonWidget extends ImageButton {
    private final AbstractContainerScreen<?> parent;
    private final Optional<Function<AbstractContainerScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableButtonWidget(int width,
                                     int height,
                                     WidgetSprites textures,
                                     OnPress pressAction,
                                     net.minecraft.network.chat.Component text,
                                     AbstractContainerScreen<?> parent,
                                     Optional<Function<AbstractContainerScreenAccessor, Vec2i>> posUpdater) {
        super(0, 0, width, height, textures, pressAction, text);
        this.parent = parent;
        this.posUpdater = posUpdater;
        Screens.getButtons(parent).add(this);
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        posUpdater.ifPresent(updater -> setPos(updater.apply((AbstractContainerScreenAccessor) parent)));
        super.renderContents(context, mouseX, mouseY, delta);
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
        private WidgetSprites textures;
        private OnPress pressAction = button -> {};
        @Nullable
        private Tooltip tooltip;
        private net.minecraft.network.chat.Component text = CommonComponents.EMPTY;
        private final AbstractContainerScreen<?> parent;
        private Optional<Function<AbstractContainerScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(AbstractContainerScreen<?> parent) {
            this.parent = parent;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setTextures(WidgetSprites textures) {
            this.textures = textures;
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setPressAction(OnPress pressAction) {
            this.pressAction = pressAction;
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setTooltip(@Nullable net.minecraft.network.chat.Component content) {
            if (content != null) {
                this.tooltip = Tooltip.create(content);
            }
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setTooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setText(net.minecraft.network.chat.Component text) {
            this.text = text;
            return this;
        }

        public io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget.Builder setPosUpdater(Function<AbstractContainerScreenAccessor, Vec2i> posUpdater) {
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
