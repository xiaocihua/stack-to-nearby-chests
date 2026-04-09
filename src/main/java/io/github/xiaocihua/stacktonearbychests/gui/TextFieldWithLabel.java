package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TextFieldWithLabel extends WBoxCustom {
    private static final int RESET_BUTTON_WIDTH = 55;
    private static final int TEXT_FIELD_WIDTH = 40;

    private final WLabel label;
    private final WTextField textField;
    private final WButton resetButton;
    private List<FormattedCharSequence> tooltip;

    public TextFieldWithLabel(Component label, int color, Supplier<Integer> onReset) {
        super(Axis.HORIZONTAL);

        this.label = new WLabel(label, color) {
            @Override
            public void addTooltip(TooltipBuilder builder) {
                if (tooltip != null) {
                    builder.add(tooltip.toArray(new FormattedCharSequence[0]));
                }
            }
        }.setVerticalAlignment(VerticalAlignment.CENTER);
        add(this.label);

        textField = new WTextField();
        add(textField);

        resetButton = new FlatColorButton(Component.translatable("stack-to-nearby-chests.options.reset"))
                .setBorder()
                .setOnClick(() -> textField.setText(String.valueOf(onReset.get())));
        add(resetButton);
    }

    public TextFieldWithLabel withTooltip(String tooltip) {
        this.tooltip = Minecraft.getInstance().font.split(Component.translatable(tooltip), 150);
        return this;
    }

    @Override
    public void layout() {
        int labelWidth = Minecraft.getInstance().font.width(label.getText().getVisualOrderText()) + 7;
        label.setSize(labelWidth, height);
        textField.setSize(TEXT_FIELD_WIDTH, height);
        resetButton.setSize(RESET_BUTTON_WIDTH, height);

        super.layout();
    }

    public WTextField getTextField() {
        return textField;
    }
}
