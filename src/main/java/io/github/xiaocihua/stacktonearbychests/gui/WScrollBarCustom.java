package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WScrollBar;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

/**
 * Copy from {@link WScrollBar}
 */
@Environment(EnvType.CLIENT)
public class WScrollBarCustom extends WScrollBar {

    public WScrollBarCustom() {
        super();
    }

    public WScrollBarCustom(Axis axis) {
        super(axis);
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.coloredRect(context, x, y, width, height, 0xFF_262626);

        if (maxValue <= 0) return;

        int handleColor;

        if (sliding) {
            handleColor = 0xFF_686868;
        } else if (isWithinBounds(mouseX, mouseY)) {
            handleColor = 0xFF_5c5c5c;
        } else {
            handleColor = 0xFF_515151;
        }

        if (axis == Axis.HORIZONTAL) {
            ScreenDrawing.coloredRect(context, x + getHandlePosition(), y, getHandleSize(), height, handleColor);

            if (isFocused()) {
                ScreenDrawing.coloredRect(context, x + getHandlePosition(), y, getHandleSize(), height, 0xFF_686868);
            }
        } else {
            ScreenDrawing.coloredRect(context, x + 1, y + 1 + getHandlePosition(), width - 2, getHandleSize(), handleColor);

            if (isFocused()) {
                ScreenDrawing.coloredRect(context, x + 1, y + 1 + getHandlePosition(), width - 2, getHandleSize(), 0xFF_686868);
            }
        }
    }

    @Override
    public int getHandleSize() {
        float percentage = (window >= maxValue) ? 1f : window / (float) maxValue;
        int bar = (axis == Axis.HORIZONTAL) ? width : height;
        int result = (int) (percentage * bar);
        if (result < 6) result = 6;
        return result;
    }
}
