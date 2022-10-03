package io.github.xiaocihua.stacktonearbychests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ModOptions {
    public static final String MOD_ID = "stack-to-nearby-chests";
    public static final Path MOD_OPTIONS_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    public static final Path OPTIONS_FILE = MOD_OPTIONS_DIR.resolve("mod-options.json");

    private static ModOptions options = read();

    public Appearance appearance = new Appearance();
    public Behavior behavior = new Behavior();
    public Keymap keymap = new Keymap();

    public static ModOptions get() {
        return options;
    }

    public static ModOptions getDefault() {
        return new ModOptions();
    }

    private static ModOptions read() {
        try (BufferedReader reader = Files.newBufferedReader(OPTIONS_FILE, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, ModOptions.class);
        } catch (IOException | JsonSyntaxException e) {
            StackToNearbyChests.LOGGER.info("Failed to read options file, creating a new one");
            ModOptions modOptions = getDefault();
            modOptions.write();
            return modOptions;
        }
    }

    public void write() {
        try {
            Files.createDirectories(OPTIONS_FILE.getParent());
            String json = new GsonBuilder().setPrettyPrinting()
                    .create()
                    .toJson(this);
            Files.writeString(OPTIONS_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            StackToNearbyChests.LOGGER.error("Failed to write options file", e);
        }
    }

    public static class Appearance {
        public MutableBoolean showStackToNearbyContainersButton = new MutableBoolean(true);
        public MutableBoolean showRestockFromNearbyContainersButton = new MutableBoolean(true);
        public MutableBoolean showQuickStackButton = new MutableBoolean(true);
        public MutableBoolean showRestockButton = new MutableBoolean(true);

        public IntOption stackToNearbyContainersButtonPosX = new IntOption(140);
        public IntOption stackToNearbyContainersButtonPosY = new IntOption(170);

        public IntOption restockFromNearbyContainersButtonPosX = new IntOption(160);
        public IntOption restockFromNearbyContainersButtonPosY  = new IntOption(170);
    }

    public static class Behavior {
        public IntOption searchInterval = new IntOption(0);

        public MutableBoolean favoriteItemStacksCannotBeThrown = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeQuickMoved = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeSwapped = new MutableBoolean(false);

        public Set<String> stackingTargets = Set.of("minecraft:chest", "minecraft:barrel", "minecraft:shulker_box", "minecraft:ender_chest", "minecraft:trapped_chest");
        public Set<String> itemsThatWillNotBeStacked = Set.of("minecraft:shulker_box");
        public Set<String> restockingSources = Set.of("minecraft:chest", "minecraft:barrel", "minecraft:shulker_box", "minecraft:ender_chest", "minecraft:trapped_chest");
        public Set<String> itemsThatWillNotBeRestocked = Set.of("minecraft:shulker_box");
    }

    public static class Keymap {
        public KeySequence stackToNearbyContainersKey = KeySequence.empty();
        public KeySequence restockFromNearbyContainersKey = KeySequence.empty();
        public KeySequence quickStackKey = KeySequence.empty();
        public KeySequence restockKey = KeySequence.empty();
        public KeySequence markAsFavoriteKey = new KeySequence(List.of(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_MOUSE_BUTTON_2 - KeySequence.MOUSE_BUTTON_CODE_OFFSET));
        public KeySequence openModOptionsScreenKey = new KeySequence(List.of(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_C));
    }

    public static class IntOption extends MutableInt {
        private int defaultValue;

        public IntOption(int value) {
            super(value);
            defaultValue = value;
        }

        public int reset() {
            setValue(defaultValue);
            return defaultValue;
        }
    }
}
