package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BlockContainerEntry extends SelectableEntryList.Entry<Identifier> {

    private final Optional<Icon> icon;
    private final Text name;

    public BlockContainerEntry(Identifier id) {
        super(id);
        Optional<Block> block = Registries.BLOCK.getOrEmpty(id);
        icon = block.map(b -> new ItemIcon(b.asItem()));
        name = block.<Text>map(Block::getName).orElse(Text.of(id.toString()));
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        int iconSize = 16;
        int inset = 6;
        int fontWidth = MinecraftClient.getInstance().textRenderer.getWidth(name.asOrderedText());
        int fontHeight = MinecraftClient.getInstance().textRenderer.fontHeight + 2;
        icon.ifPresent(i -> i.paint(matrices, x + inset, y + (height - iconSize) / 2, 16));
        ScreenDrawing.drawString(matrices, name.asOrderedText(), x + width - inset - fontWidth, y + (height - fontHeight) / 2 + 2, TEXT_COLOR);
    }
}
