package io.github.xiaocihua.stacktonearbychests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
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
            return new GsonBuilder().registerTypeAdapter(Identifier.class, new IdentifierAdapter().nullSafe())
                    .create()
                    .fromJson(reader, ModOptions.class);
        } catch (IOException | JsonSyntaxException e) {
            StackToNearbyChests.LOGGER.info("Failed to read options file, creating a new one", e);
            ModOptions modOptions = getDefault();
            modOptions.write();
            return modOptions;
        }
    }

    public void write() {
        try {
            Files.createDirectories(OPTIONS_FILE.getParent());
            String json = new GsonBuilder().registerTypeAdapter(Identifier.class, new IdentifierAdapter().nullSafe())
                    .setPrettyPrinting()
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

        public MutableBoolean showButtonTooltip = new MutableBoolean(true);

        public IntOption stackToNearbyContainersButtonPosX = new IntOption(140);
        public IntOption stackToNearbyContainersButtonPosY = new IntOption(170);

        public IntOption restockFromNearbyContainersButtonPosX = new IntOption(160);
        public IntOption restockFromNearbyContainersButtonPosY  = new IntOption(170);

        public IntOption quickStackButtonPosX = new IntOption(6);
        public IntOption quickStackButtonPosY = new IntOption(-10);

        public IntOption restockButtonPosX = new IntOption(6);
        public IntOption restockButtonPosY = new IntOption(10);

        public Identifier favoriteItemStyle = new Identifier(ModOptions.MOD_ID, "gold_badge");
    }

    public static class Behavior {
        public IntOption searchInterval = new IntOption(0);

        public MutableBoolean supportForContainerEntities = new MutableBoolean(true);

        public MutableBoolean doNotQuickStackItemsFromTheHotbar = new MutableBoolean(false);
        public MutableBoolean favoriteItemsCannotBePickedUp = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeThrown = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeQuickMoved = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeSwapped = new MutableBoolean(false);
        public MutableBoolean favoriteItemsCannotBeSwappedWithOffhand = new MutableBoolean(false);

        public Set<String> stackingTargets = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box",
                "minecraft:barrel",
                "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box",
                "minecraft:trapped_chest",
                "minecraft:ender_chest",
                "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box",
                "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box",
                "minecraft:chest",
                "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box",
                "charm:warped_chest",
                "charm:crimson_barrel",
                "charm:spruce_chest",
                "charm:azalea_barrel",
                "charm:spruce_trapped_chest",
                "charm:mangrove_chest",
                "charm:spruce_barrel",
                "charm:jungle_chest",
                "charm:acacia_chest",
                "charm:ebony_trapped_chest",
                "charm:oak_chest",
                "charm:dark_oak_barrel",
                "charm:jungle_barrel",
                "charm:dark_oak_trapped_chest",
                "charm:azalea_trapped_chest",
                "charm:mangrove_barrel",
                "charm:warped_barrel",
                "charm:azalea_chest",
                "charm:acacia_barrel",
                "charm:crimson_trapped_chest",
                "charm:warped_trapped_chest",
                "charm:ebony_chest",
                "charm:ebony_barrel",
                "charm:birch_barrel",
                "charm:birch_trapped_chest",
                "charm:birch_chest",
                "charm:dark_oak_chest",
                "charm:oak_trapped_chest",
                "charm:oak_barrel",
                "charm:crimson_chest",
                "charm:mangrove_trapped_chest",
                "charm:acacia_trapped_chest",
                "charm:jungle_trapped_chest",
                "expandedstorage:bamboo_chest",
                "expandedstorage:candy_cane_mini_present",
                "expandedstorage:copper_barrel",
                "expandedstorage:copper_mini_barrel",
                "expandedstorage:diamond_barrel",
                "expandedstorage:diamond_chest",
                "expandedstorage:diamond_mini_barrel",
                "expandedstorage:diamond_mini_chest",
                "expandedstorage:exposed_copper_barrel",
                "expandedstorage:exposed_copper_mini_barrel",
                "expandedstorage:gold_barrel",
                "expandedstorage:gold_chest",
                "expandedstorage:gold_mini_barrel",
                "expandedstorage:gold_mini_chest",
                "expandedstorage:green_mini_present",
                "expandedstorage:iron_barrel",
                "expandedstorage:iron_chest",
                "expandedstorage:iron_mini_barrel",
                "expandedstorage:iron_mini_chest",
                "expandedstorage:lavender_mini_present",
                "expandedstorage:mini_barrel",
                "expandedstorage:moss_chest",
                "expandedstorage:netherite_barrel",
                "expandedstorage:netherite_chest",
                "expandedstorage:netherite_mini_barrel",
                "expandedstorage:netherite_mini_chest",
                "expandedstorage:obsidian_barrel",
                "expandedstorage:obsidian_chest",
                "expandedstorage:obsidian_mini_barrel",
                "expandedstorage:obsidian_mini_chest",
                "expandedstorage:old_diamond_chest",
                "expandedstorage:old_gold_chest",
                "expandedstorage:old_iron_chest",
                "expandedstorage:old_netherite_chest",
                "expandedstorage:old_obsidian_chest",
                "expandedstorage:old_wood_chest",
                "expandedstorage:oxidized_copper_barrel",
                "expandedstorage:oxidized_copper_mini_barrel",
                "expandedstorage:pink_amethyst_mini_present",
                "expandedstorage:present",
                "expandedstorage:pumpkin_chest",
                "expandedstorage:pumpkin_mini_chest",
                "expandedstorage:red_mini_present",
                "expandedstorage:vanilla_wood_mini_chest",
                "expandedstorage:waxed_copper_barrel",
                "expandedstorage:waxed_copper_mini_barrel",
                "expandedstorage:waxed_exposed_copper_barrel",
                "expandedstorage:waxed_exposed_copper_mini_barrel",
                "expandedstorage:waxed_oxidized_copper_barrel",
                "expandedstorage:waxed_oxidized_copper_mini_barrel",
                "expandedstorage:waxed_weathered_copper_barrel",
                "expandedstorage:waxed_weathered_copper_mini_barrel",
                "expandedstorage:weathered_copper_barrel",
                "expandedstorage:weathered_copper_mini_barrel",
                "expandedstorage:white_mini_present",
                "expandedstorage:wood_chest",
                "expandedstorage:wood_mini_chest");
        public Set<String> stackingTargetEntities = Set.of("minecraft:chest_boat",
                "minecraft:trader_llama",
                "minecraft:chest_minecart",
                "minecraft:donkey",
                "minecraft:llama",
                "minecraft:mule");
        public Set<String> itemsThatWillNotBeStacked = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box",
                "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box",
                "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box",
                "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box",
                "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box");
        public Set<String> restockingSources = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box",
                "minecraft:barrel",
                "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box",
                "minecraft:trapped_chest",
                "minecraft:ender_chest",
                "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box",
                "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box",
                "minecraft:chest",
                "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box",
                "charm:warped_chest",
                "charm:crimson_barrel",
                "charm:spruce_chest",
                "charm:azalea_barrel",
                "charm:spruce_trapped_chest",
                "charm:mangrove_chest",
                "charm:spruce_barrel",
                "charm:jungle_chest",
                "charm:acacia_chest",
                "charm:ebony_trapped_chest",
                "charm:oak_chest",
                "charm:dark_oak_barrel",
                "charm:jungle_barrel",
                "charm:dark_oak_trapped_chest",
                "charm:azalea_trapped_chest",
                "charm:mangrove_barrel",
                "charm:warped_barrel",
                "charm:azalea_chest",
                "charm:acacia_barrel",
                "charm:crimson_trapped_chest",
                "charm:warped_trapped_chest",
                "charm:ebony_chest",
                "charm:ebony_barrel",
                "charm:birch_barrel",
                "charm:birch_trapped_chest",
                "charm:birch_chest",
                "charm:dark_oak_chest",
                "charm:oak_trapped_chest",
                "charm:oak_barrel",
                "charm:crimson_chest",
                "charm:mangrove_trapped_chest",
                "charm:acacia_trapped_chest",
                "charm:jungle_trapped_chest",
                "expandedstorage:bamboo_chest",
                "expandedstorage:candy_cane_mini_present",
                "expandedstorage:copper_barrel",
                "expandedstorage:copper_mini_barrel",
                "expandedstorage:diamond_barrel",
                "expandedstorage:diamond_chest",
                "expandedstorage:diamond_mini_barrel",
                "expandedstorage:diamond_mini_chest",
                "expandedstorage:exposed_copper_barrel",
                "expandedstorage:exposed_copper_mini_barrel",
                "expandedstorage:gold_barrel",
                "expandedstorage:gold_chest",
                "expandedstorage:gold_mini_barrel",
                "expandedstorage:gold_mini_chest",
                "expandedstorage:green_mini_present",
                "expandedstorage:iron_barrel",
                "expandedstorage:iron_chest",
                "expandedstorage:iron_mini_barrel",
                "expandedstorage:iron_mini_chest",
                "expandedstorage:lavender_mini_present",
                "expandedstorage:mini_barrel",
                "expandedstorage:moss_chest",
                "expandedstorage:netherite_barrel",
                "expandedstorage:netherite_chest",
                "expandedstorage:netherite_mini_barrel",
                "expandedstorage:netherite_mini_chest",
                "expandedstorage:obsidian_barrel",
                "expandedstorage:obsidian_chest",
                "expandedstorage:obsidian_mini_barrel",
                "expandedstorage:obsidian_mini_chest",
                "expandedstorage:old_diamond_chest",
                "expandedstorage:old_gold_chest",
                "expandedstorage:old_iron_chest",
                "expandedstorage:old_netherite_chest",
                "expandedstorage:old_obsidian_chest",
                "expandedstorage:old_wood_chest",
                "expandedstorage:oxidized_copper_barrel",
                "expandedstorage:oxidized_copper_mini_barrel",
                "expandedstorage:pink_amethyst_mini_present",
                "expandedstorage:present",
                "expandedstorage:pumpkin_chest",
                "expandedstorage:pumpkin_mini_chest",
                "expandedstorage:red_mini_present",
                "expandedstorage:vanilla_wood_mini_chest",
                "expandedstorage:waxed_copper_barrel",
                "expandedstorage:waxed_copper_mini_barrel",
                "expandedstorage:waxed_exposed_copper_barrel",
                "expandedstorage:waxed_exposed_copper_mini_barrel",
                "expandedstorage:waxed_oxidized_copper_barrel",
                "expandedstorage:waxed_oxidized_copper_mini_barrel",
                "expandedstorage:waxed_weathered_copper_barrel",
                "expandedstorage:waxed_weathered_copper_mini_barrel",
                "expandedstorage:weathered_copper_barrel",
                "expandedstorage:weathered_copper_mini_barrel",
                "expandedstorage:white_mini_present",
                "expandedstorage:wood_chest",
                "expandedstorage:wood_mini_chest");
        public Set<String> restockingSourceEntities = Set.of("minecraft:chest_boat",
                "minecraft:trader_llama",
                "minecraft:chest_minecart",
                "minecraft:donkey",
                "minecraft:llama",
                "minecraft:mule");
        public Set<String> itemsThatWillNotBeRestocked = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box",
                "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box",
                "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box",
                "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box",
                "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box");
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
        private final int defaultValue;

        public IntOption(int value) {
            super(value);
            defaultValue = value;
        }

        public int reset() {
            setValue(defaultValue);
            return defaultValue;
        }
    }

    public static class IdentifierAdapter extends TypeAdapter<Identifier> {

        @Override
        public Identifier read(JsonReader in) throws IOException {
            return new Identifier(in.nextString());
        }

        @Override
        public void write(JsonWriter out, Identifier value) throws IOException {
            out.value(value.toString());
        }
    }
}
