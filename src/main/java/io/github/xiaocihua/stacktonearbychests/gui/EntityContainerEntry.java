package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EntityContainerEntry extends SelectableEntryList.Entry<Identifier>{

    private final Text name;

    public EntityContainerEntry(Identifier id) {
        super(id);
        name = Registries.ENTITY_TYPE.getOptionalValue(id).map(EntityType::getName).orElse(Text.of(id.toString()));
    }

    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        int inset = 6;
        int fontWidth = MinecraftClient.getInstance().textRenderer.getWidth(name.asOrderedText());
        int fontHeight = MinecraftClient.getInstance().textRenderer.fontHeight + 2;
        ScreenDrawing.drawString(context, name.asOrderedText(), x + width - inset - fontWidth, y + (height - fontHeight) / 2 + 2, ModOptionsGui.TEXT_COLOR);
    }
}
