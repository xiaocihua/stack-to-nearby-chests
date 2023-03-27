package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import io.github.xiaocihua.stacktonearbychests.KeySequence;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeymapEntry extends WBox {
    private static final int KEYBINDING_WIDTH = 170;
    private static final int RESET_BUTTON_WIDTH = 40;

    private final WLabel label;
    private final KeyBindingWidget keybinding;
    private final WButton resetButton;

    public KeymapEntry(Text label, KeySequence keySequence) {
        super(Axis.HORIZONTAL);

        this.label = new WLabel(label, ModOptionsGui.TEXT_COLOR).setVerticalAlignment(VerticalAlignment.CENTER);
        add(this.label);

        keybinding = new KeyBindingWidget(keySequence);
        add(keybinding);

        resetButton = new FlatColorButton(Text.translatable("stack-to-nearby-chests.options.reset"))
                .setBorder()
                .setOnClick(keybinding::reset);
        add(resetButton);
    }

    @Override
    public void layout() {
        label.setSize(width - KEYBINDING_WIDTH - RESET_BUTTON_WIDTH - spacing, height);
        keybinding.setSize(KEYBINDING_WIDTH - spacing, height);
        resetButton.setSize(RESET_BUTTON_WIDTH, height);
        super.layout();
    }

    private static class KeyBindingWidget extends FlatColorButton {

        private final KeySequence keySequence;

        public KeyBindingWidget(KeySequence keySequence) {
            super(0xFF_262626, 0x00_000000, 0x00_000000);
            this.keySequence = keySequence;
            setLabel();
        }

        public void reset() {
            this.keySequence.reset();
            setLabel();
        }

        @Override
        public InputResult onClick(int x, int y, int button) {
            if (isFocused()) {
                keySequence.addMouseButton(button);
            } else {
                requestFocus();
            }
            setLabel();

            return InputResult.PROCESSED;
        }

        @Override
        public InputResult onKeyPressed(int ch, int key, int modifiers) {
            switch (ch) {
                case GLFW.GLFW_KEY_ENTER -> releaseFocus();
                case GLFW.GLFW_KEY_BACKSPACE -> keySequence.clear();
                default -> keySequence.addKey(ch);
            }
            setLabel();

            return InputResult.PROCESSED;
        }

        private void setLabel() {
            setLabel(keySequence.getLocalizedText());
        }

        @Override
        public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
            if (isFocused()) {
                drawBorder(matrices, x, y, width, height, 0xFF_F5F5F5);
            }

            super.paint(matrices, x, y, mouseX, mouseY);
        }
    }
}
