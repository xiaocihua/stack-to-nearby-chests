package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class EntityContainerEntry extends SelectableEntryList.Entry<Identifier>{

    private final Component name;

    public EntityContainerEntry(Identifier id) {
        super(id);
        name = BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(EntityType::getDescription).orElse(Component.nullToEmpty(id.toString()));
    }

    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        int inset = 6;
        int fontWidth = Minecraft.getInstance().font.width(name.getVisualOrderText());
        int fontHeight = Minecraft.getInstance().font.lineHeight + 2;
        ScreenDrawing.drawString(context, name.getVisualOrderText(), x + width - inset - fontWidth, y + (height - fontHeight) / 2 + 2, TEXT_COLOR);
    }
}
