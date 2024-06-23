package io.github.xiaocihua.stacktonearbychests.compat;

import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

public class SophisticatedStorageCompat {

    private static final String MOD_ID = "sophisticatedstorage";
    private static final boolean IS_SOPHISTICATED_STORAGE_MOD_LOADED = FabricLoader.getInstance().isModLoaded(MOD_ID);

    public static boolean isModContainer(Block block) {
        if (IS_SOPHISTICATED_STORAGE_MOD_LOADED) {
            try {
                Class<?> clazz = Class.forName("net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase");
                return clazz.isInstance(block);
            } catch (ClassNotFoundException e) {
                StackToNearbyChests.LOGGER.error(e);
            }
        }

        return false;
    }

    public static boolean isModBlock(Block block) {
        return IS_SOPHISTICATED_STORAGE_MOD_LOADED && Registries.BLOCK.getId(block).getNamespace().equals(MOD_ID);
    }
}
