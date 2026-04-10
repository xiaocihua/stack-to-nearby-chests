package io.github.xiaocihua.stacktonearbychests.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public class TravelersBackpackMod {
    public static final String MOD_ID = "travelersbackpack";
    public static final String CONTAINER_SCREEN_CLASS_NAME = "com.tiviacz.travelersbackpack.client.screens.BackpackScreen";

    private static TravelersBackpackMod instance;

    private final boolean modLoaded;

    public static TravelersBackpackMod getInstance() {
        if (instance == null) {
            instance = new TravelersBackpackMod();
        }
        return instance;
    }

    private TravelersBackpackMod() {
        this.modLoaded = FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    public boolean isContainerScreen(Screen screen) {
        return isModLoaded() && CONTAINER_SCREEN_CLASS_NAME.equals(screen.getClass().getCanonicalName());
    }

    public boolean isModLoaded() {
        return modLoaded;
    }
}
