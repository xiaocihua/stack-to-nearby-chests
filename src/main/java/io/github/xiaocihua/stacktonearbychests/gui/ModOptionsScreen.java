package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModOptionsScreen extends CottonClientScreen {

    public ModOptionsScreen(GuiDescription description) {
        super(description);
    }
}
