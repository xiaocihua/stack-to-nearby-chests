package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ItemEntry extends SelectableEntryList.Entry<Identifier> {

    private final Icon icon;
    private final Component name;

    public ItemEntry(Identifier id) {
        super(id);
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
        icon = item.<Icon>map(ItemIcon::new).orElse(new TextureIcon(MissingTextureAtlasSprite.getLocation()));
        name = item.map(Item::getName).orElse(Component.nullToEmpty(id.toString()));
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        int iconSize = 16;
        int inset = 6;
        int fontWidth = Minecraft.getInstance().font.width(name.getVisualOrderText());
        int fontHeight = Minecraft.getInstance().font.lineHeight + 2;
        icon.paint(context, x + inset, y + (height - iconSize) / 2, 16);
        ScreenDrawing.drawString(context, name.getVisualOrderText(), x + width - inset - fontWidth, y + (height - fontHeight) / 2 + 2, TEXT_COLOR);
    }
}
