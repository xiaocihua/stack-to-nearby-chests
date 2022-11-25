package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TextFieldWithLabel extends WBoxCustom {
    private static final int RESET_BUTTON_WIDTH = 40;

    private final WLabel label;
    private final WTextField textField;
    private final WButton resetButton;
    private List<OrderedText> tooltip;

    public TextFieldWithLabel(Text label, int color, Supplier<Integer> onReset) {
        super(Axis.HORIZONTAL);

        this.label = new WLabel(label, color) {
            @Override
            public void addTooltip(TooltipBuilder builder) {
                if (tooltip != null) {
                    builder.add(tooltip.toArray(new OrderedText[0]));
                }
            }
        }.setVerticalAlignment(VerticalAlignment.CENTER);
        add(this.label);

        textField = new WTextField();
        add(textField);

        resetButton = new FlatColorButton(new TranslatableText("stack-to-nearby-chests.options.reset"))
                .setBorder()
                .setOnClick(() -> textField.setText(String.valueOf(onReset.get())));
        add(resetButton);
    }

    public TextFieldWithLabel withTooltip(String tooltip) {
        this.tooltip = MinecraftClient.getInstance().textRenderer.wrapLines(new TranslatableText(tooltip), 150);
        return this;
    }

    @Override
    public void layout() {
        int labelWidth = MinecraftClient.getInstance().textRenderer.getWidth(label.getText().asOrderedText()) + 7;
        label.setSize(labelWidth, height);
        textField.setSize(width - labelWidth - RESET_BUTTON_WIDTH, height);
        resetButton.setSize(RESET_BUTTON_WIDTH, height);

        super.layout();
    }

    public WTextField getTextField() {
        return textField;
    }
}
