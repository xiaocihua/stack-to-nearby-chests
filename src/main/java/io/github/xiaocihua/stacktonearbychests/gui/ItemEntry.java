package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ItemEntry extends SelectableEntryList.Entry<Identifier> {

    private final Icon icon;
    private final Text name;

    public ItemEntry(Identifier id) {
        super(id);
        Optional<Item> item = Registries.ITEM.getOptionalValue(id);
        icon = item.<Icon>map(ItemIcon::new).orElse(new TextureIcon(MissingSprite.getMissingSpriteId()));
        name = item.map(Item::getName).orElse(Text.of(id.toString()));
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        int iconSize = 16;
        int inset = 6;
        int fontWidth = MinecraftClient.getInstance().textRenderer.getWidth(name.asOrderedText());
        int fontHeight = MinecraftClient.getInstance().textRenderer.fontHeight + 2;
        icon.paint(context, x + inset, y + (height - iconSize) / 2, 16);
        ScreenDrawing.drawString(context, name.asOrderedText(), x + width - inset - fontWidth, y + (height - fontHeight) / 2 + 2, TEXT_COLOR);
    }
}
