package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.OptionalInt;

public class FlatColorButton extends WButton {

    private int regularColor = 0x00_000000;
    private int hoveredColor = 0x14_FFFFFF;
    private int disabledColor = 0x00_000000;

    private OptionalInt borderColor = OptionalInt.empty();

    public FlatColorButton() {}

    public FlatColorButton(Text text) {
        super(text);
    }

    public FlatColorButton(Icon icon, Text text) {
        super(icon, text);
    }

    public FlatColorButton(int regularColor, int hoveredColor, int disabledColor) {
        this.regularColor = regularColor;
        this.hoveredColor = hoveredColor;
        this.disabledColor = disabledColor;
    }

    public FlatColorButton(Text text, int regularColor, int hoveredColor, int disabledColor) {
        super(text);
        this.regularColor = regularColor;
        this.hoveredColor = hoveredColor;
        this.disabledColor = disabledColor;
    }

    public FlatColorButton setBorder() {
        return setBorder(0xFF_717171);
    }

    public FlatColorButton setBorder(int color) {
        borderColor = OptionalInt.of(color);
        return this;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
        boolean enabled = isEnabled();
        Icon icon = getIcon();
        Text label = getLabel();

        if (!enabled) {
            ScreenDrawing.coloredRect(context, x, y, width, height, disabledColor);
        } else if (hovered || isFocused()) {
            ScreenDrawing.coloredRect(context, x, y, width, height, hoveredColor);
        } else {
            ScreenDrawing.coloredRect(context, x, y, width, height, regularColor);
        }

        borderColor.ifPresent(color -> drawBorder(context, x, y, width, height, color));

        if (icon != null) {
            icon.paint(context, x + 2, y + 2, 16);
        }

        if (label != null) {
            int color = ModOptionsGui.BUTTON_LABEL_COLOR;
            if (!enabled) {
                color = ModOptionsGui.BUTTON_LABEL_COLOR_DISABLED;
            } /*else if (hovered) {
				color = 0xFFFFFFA0;
			}*/

            int xOffset = (icon != null && alignment == HorizontalAlignment.LEFT) ? 18 : 0;
            ScreenDrawing.drawStringWithShadow(context, label.asOrderedText(), alignment, x + xOffset, y + ((height - 8) / 2), width, color); //LibGuiClient.config.darkMode ? darkmodeColor : color);
        }
    }

    protected void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        ScreenDrawing.coloredRect(context, x, y, width, 1, color);
        ScreenDrawing.coloredRect(context, x, y + height - 1, width, 1, color);
        ScreenDrawing.coloredRect(context, x, y, 1, height, color);
        ScreenDrawing.coloredRect(context, x + width - 1, y, 1, height, color);
    }
}
