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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER;

@Environment(EnvType.CLIENT)
public class ModOptions {
    public static final String MOD_ID = "stack-to-nearby-chests";
    public static final Path MOD_OPTIONS_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    public static final Path OPTIONS_FILE = MOD_OPTIONS_DIR.resolve("mod-options.json");

    private static final ModOptions options = read();

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
        } catch (NoSuchFileException e) {
            LOGGER.info("Options file does not exist, creating a new one");
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.info("Failed to read options file, creating a new one", e);
        }

        ModOptions modOptions = getDefault();
        modOptions.write();
        return modOptions;
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
            LOGGER.error("Failed to write options file", e);
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

        public MutableBoolean alwaysShowMarkersForFavoritedItems = new MutableBoolean(true);

        public MutableBoolean showTheButtonsOnTheCreativeInventoryScreen = new MutableBoolean(true);
    }

    public static class Behavior {
        public IntOption searchInterval = new IntOption(0);

        public MutableBoolean supportForContainerEntities = new MutableBoolean(true);

        public MutableBoolean doNotQuickStackItemsFromTheHotbar = new MutableBoolean(false);

        public MutableBoolean enableItemFavoriting = new MutableBoolean(true);
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
                "ironchest:copper_chest",
                "ironchest:iron_chest",
                "ironchest:gold_chest",
                "ironchest:diamond_chest",
                "expandedstorage:wood_chest",
                "expandedstorage:pumpkin_chest",
                "expandedstorage:present",
                "expandedstorage:bamboo_chest",
                "expandedstorage:moss_chest",
                "expandedstorage:iron_chest",
                "expandedstorage:gold_chest",
                "expandedstorage:diamond_chest",
                "expandedstorage:obsidian_chest",
                "expandedstorage:netherite_chest",
                "expandedstorage:old_wood_chest",
                "expandedstorage:old_iron_chest",
                "expandedstorage:old_gold_chest",
                "expandedstorage:old_diamond_chest",
                "expandedstorage:old_obsidian_chest",
                "expandedstorage:old_netherite_chest",
                "expandedstorage:copper_barrel",
                "expandedstorage:exposed_copper_barrel",
                "expandedstorage:weathered_copper_barrel",
                "expandedstorage:oxidized_copper_barrel",
                "expandedstorage:waxed_copper_barrel",
                "expandedstorage:waxed_exposed_copper_barrel",
                "expandedstorage:waxed_weathered_copper_barrel",
                "expandedstorage:waxed_oxidized_copper_barrel",
                "expandedstorage:iron_barrel",
                "expandedstorage:gold_barrel",
                "expandedstorage:diamond_barrel",
                "expandedstorage:obsidian_barrel",
                "expandedstorage:netherite_barrel",
                "expandedstorage:vanilla_wood_mini_chest",
                "expandedstorage:wood_mini_chest",
                "expandedstorage:pumpkin_mini_chest",
                "expandedstorage:red_mini_present",
                "expandedstorage:white_mini_present",
                "expandedstorage:candy_cane_mini_present",
                "expandedstorage:green_mini_present",
                "expandedstorage:lavender_mini_present",
                "expandedstorage:pink_amethyst_mini_present",
                "expandedstorage:iron_mini_chest",
                "expandedstorage:gold_mini_chest",
                "expandedstorage:diamond_mini_chest",
                "expandedstorage:obsidian_mini_chest",
                "expandedstorage:netherite_mini_chest",
                "expandedstorage:copper_mini_barrel",
                "expandedstorage:exposed_copper_mini_barrel",
                "expandedstorage:weathered_copper_mini_barrel",
                "expandedstorage:oxidized_copper_mini_barrel",
                "expandedstorage:waxed_copper_mini_barrel",
                "expandedstorage:waxed_exposed_copper_mini_barrel",
                "expandedstorage:waxed_weathered_copper_mini_barrel",
                "expandedstorage:waxed_oxidized_copper_mini_barrel",
                "expandedstorage:mini_barrel",
                "expandedstorage:iron_mini_barrel",
                "expandedstorage:gold_mini_barrel",
                "expandedstorage:diamond_mini_barrel",
                "expandedstorage:obsidian_mini_barrel",
                "expandedstorage:netherite_mini_barrel",
                "compact_storage:compact_chest_white",
                "compact_storage:compact_chest_orange",
                "compact_storage:compact_chest_magenta",
                "compact_storage:compact_chest_light_blue",
                "compact_storage:compact_chest_yellow",
                "compact_storage:compact_chest_lime",
                "compact_storage:compact_chest_pink",
                "compact_storage:compact_chest_gray",
                "compact_storage:compact_chest_light_gray",
                "compact_storage:compact_chest_cyan",
                "compact_storage:compact_chest_purple",
                "compact_storage:compact_chest_blue",
                "compact_storage:compact_chest_brown",
                "compact_storage:compact_chest_green",
                "compact_storage:compact_chest_red",
                "compact_storage:compact_chest_black",
                "compact_storage:compact_barrel_white",
                "compact_storage:compact_barrel_orange",
                "compact_storage:compact_barrel_magenta",
                "compact_storage:compact_barrel_light_blue",
                "compact_storage:compact_barrel_yellow",
                "compact_storage:compact_barrel_lime",
                "compact_storage:compact_barrel_pink",
                "compact_storage:compact_barrel_gray",
                "compact_storage:compact_barrel_light_gray",
                "compact_storage:compact_barrel_cyan",
                "compact_storage:compact_barrel_purple",
                "compact_storage:compact_barrel_blue",
                "compact_storage:compact_barrel_brown",
                "compact_storage:compact_barrel_green",
                "compact_storage:compact_barrel_red",
                "compact_storage:compact_barrel_black",
                "betterend:tenanea_chest",
                "betterend:mossy_glowshroom_chest",
                "betterend:pythadendron_chest",
                "betterend:end_lotus_chest",
                "betterend:lacugrove_chest",
                "betterend:dragon_tree_chest",
                "betterend:helix_tree_chest",
                "betterend:umbrella_tree_chest",
                "betterend:jellyshroom_chest",
                "betternether:rubeus_chest",
                "betterend:lucernia_chest",
                "betternether:nether_reed_chest",
                "betternether:stalagnate_chest",
                "betternether:willow_chest",
                "betternether:wart_chest",
                "betternether:chest_of_drawers",
                "betternether:crimson_chest",
                "betternether:warped_chest",
                "betternether:mushroom_fir_chest",
                "betternether:nether_mushroom_chest",
                "betternether:anchor_tree_chest",
                "betternether:nether_sakura_chest",
                "ironchests:diamond_chest",
                "ironchests:iron_chest",
                "ironchests:gold_chest",
                "ironchests:crystal_chest",
                "ironchests:copper_chest",
                "ironchests:dirt_chest",
                "ironchests:obsidian_chest",
                "ironchests:netherite_chest",
                "ironchests:iron_barrel",
                "ironchests:gold_barrel",
                "ironchests:crystal_barrel",
                "ironchests:copper_barrel",
                "ironchests:obsidian_barrel",
                "ironchests:netherite_barrel",
                "reinfchest:copper_chest",
                "reinfchest:iron_chest",
                "reinfchest:gold_chest",
                "reinfchest:diamond_chest",
                "reinfchest:netherite_chest",
                "reinfbarrel:copper_barrel",
                "reinfbarrel:iron_barrel",
                "reinfbarrel:gold_barrel",
                "reinfbarrel:diamond_barrel",
                "reinfbarrel:netherite_barrel",
                "reinfshulker:copper_shulker_box",
                "reinfshulker:white_copper_shulker_box",
                "reinfshulker:orange_copper_shulker_box",
                "reinfshulker:magenta_copper_shulker_box",
                "reinfshulker:light_blue_copper_shulker_box",
                "reinfshulker:yellow_copper_shulker_box",
                "reinfshulker:lime_copper_shulker_box",
                "reinfshulker:pink_copper_shulker_box",
                "reinfshulker:gray_copper_shulker_box",
                "reinfshulker:light_gray_copper_shulker_box",
                "reinfshulker:cyan_copper_shulker_box",
                "reinfshulker:purple_copper_shulker_box",
                "reinfshulker:blue_copper_shulker_box",
                "reinfshulker:brown_copper_shulker_box",
                "reinfshulker:green_copper_shulker_box",
                "reinfshulker:red_copper_shulker_box",
                "reinfshulker:black_copper_shulker_box",
                "reinfshulker:iron_shulker_box",
                "reinfshulker:white_iron_shulker_box",
                "reinfshulker:orange_iron_shulker_box",
                "reinfshulker:magenta_iron_shulker_box",
                "reinfshulker:light_blue_iron_shulker_box",
                "reinfshulker:yellow_iron_shulker_box",
                "reinfshulker:lime_iron_shulker_box",
                "reinfshulker:pink_iron_shulker_box",
                "reinfshulker:gray_iron_shulker_box",
                "reinfshulker:light_gray_iron_shulker_box",
                "reinfshulker:cyan_iron_shulker_box",
                "reinfshulker:purple_iron_shulker_box",
                "reinfshulker:blue_iron_shulker_box",
                "reinfshulker:brown_iron_shulker_box",
                "reinfshulker:green_iron_shulker_box",
                "reinfshulker:red_iron_shulker_box",
                "reinfshulker:black_iron_shulker_box",
                "reinfshulker:gold_shulker_box",
                "reinfshulker:white_gold_shulker_box",
                "reinfshulker:orange_gold_shulker_box",
                "reinfshulker:magenta_gold_shulker_box",
                "reinfshulker:light_blue_gold_shulker_box",
                "reinfshulker:yellow_gold_shulker_box",
                "reinfshulker:lime_gold_shulker_box",
                "reinfshulker:pink_gold_shulker_box",
                "reinfshulker:gray_gold_shulker_box",
                "reinfshulker:light_gray_gold_shulker_box",
                "reinfshulker:cyan_gold_shulker_box",
                "reinfshulker:purple_gold_shulker_box",
                "reinfshulker:blue_gold_shulker_box",
                "reinfshulker:brown_gold_shulker_box",
                "reinfshulker:green_gold_shulker_box",
                "reinfshulker:red_gold_shulker_box",
                "reinfshulker:black_gold_shulker_box",
                "reinfshulker:diamond_shulker_box",
                "reinfshulker:white_diamond_shulker_box",
                "reinfshulker:orange_diamond_shulker_box",
                "reinfshulker:magenta_diamond_shulker_box",
                "reinfshulker:light_blue_diamond_shulker_box",
                "reinfshulker:yellow_diamond_shulker_box",
                "reinfshulker:lime_diamond_shulker_box",
                "reinfshulker:pink_diamond_shulker_box",
                "reinfshulker:gray_diamond_shulker_box",
                "reinfshulker:light_gray_diamond_shulker_box",
                "reinfshulker:cyan_diamond_shulker_box",
                "reinfshulker:purple_diamond_shulker_box",
                "reinfshulker:blue_diamond_shulker_box",
                "reinfshulker:brown_diamond_shulker_box",
                "reinfshulker:green_diamond_shulker_box",
                "reinfshulker:red_diamond_shulker_box",
                "reinfshulker:black_diamond_shulker_box",
                "reinfshulker:netherite_shulker_box",
                "reinfshulker:white_netherite_shulker_box",
                "reinfshulker:orange_netherite_shulker_box",
                "reinfshulker:magenta_netherite_shulker_box",
                "reinfshulker:light_blue_netherite_shulker_box",
                "reinfshulker:yellow_netherite_shulker_box",
                "reinfshulker:lime_netherite_shulker_box",
                "reinfshulker:pink_netherite_shulker_box",
                "reinfshulker:gray_netherite_shulker_box",
                "reinfshulker:light_gray_netherite_shulker_box",
                "reinfshulker:cyan_netherite_shulker_box",
                "reinfshulker:purple_netherite_shulker_box",
                "reinfshulker:blue_netherite_shulker_box",
                "reinfshulker:brown_netherite_shulker_box",
                "reinfshulker:green_netherite_shulker_box",
                "reinfshulker:red_netherite_shulker_box",
                "reinfshulker:black_netherite_shulker_box",
                "sophisticatedstorage:chest",
                "sophisticatedstorage:iron_chest",
                "sophisticatedstorage:gold_chest",
                "sophisticatedstorage:diamond_chest",
                "sophisticatedstorage:netherite_chest",
                "sophisticatedstorage:barrel",
                "sophisticatedstorage:iron_barrel",
                "sophisticatedstorage:gold_barrel",
                "sophisticatedstorage:diamond_barrel",
                "sophisticatedstorage:netherite_barrel",
                "sophisticatedstorage:shulker_box",
                "sophisticatedstorage:iron_shulker_box",
                "sophisticatedstorage:gold_shulker_box",
                "sophisticatedstorage:diamond_shulker_box",
                "sophisticatedstorage:netherite_shulker_box");
        public Set<String> stackingTargetEntities = Set.of("minecraft:chest_boat",
                "minecraft:trader_llama",
                "minecraft:chest_minecart",
                "minecraft:donkey",
                "minecraft:llama",
                "minecraft:mule",
                "expandedstorage:wood_chest_minecart",
                "expandedstorage:pumpkin_chest_minecart",
                "expandedstorage:present_minecart",
                "expandedstorage:bamboo_chest_minecart",
                "expandedstorage:moss_chest_minecart",
                "expandedstorage:iron_chest_minecart",
                "expandedstorage:gold_chest_minecart",
                "expandedstorage:diamond_chest_minecart",
                "expandedstorage:obsidian_chest_minecart",
                "expandedstorage:netherite_chest_minecart");
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
                "ironchest:copper_chest",
                "ironchest:iron_chest",
                "ironchest:gold_chest",
                "ironchest:diamond_chest",
                "expandedstorage:wood_chest",
                "expandedstorage:pumpkin_chest",
                "expandedstorage:present",
                "expandedstorage:bamboo_chest",
                "expandedstorage:moss_chest",
                "expandedstorage:iron_chest",
                "expandedstorage:gold_chest",
                "expandedstorage:diamond_chest",
                "expandedstorage:obsidian_chest",
                "expandedstorage:netherite_chest",
                "expandedstorage:old_wood_chest",
                "expandedstorage:old_iron_chest",
                "expandedstorage:old_gold_chest",
                "expandedstorage:old_diamond_chest",
                "expandedstorage:old_obsidian_chest",
                "expandedstorage:old_netherite_chest",
                "expandedstorage:copper_barrel",
                "expandedstorage:exposed_copper_barrel",
                "expandedstorage:weathered_copper_barrel",
                "expandedstorage:oxidized_copper_barrel",
                "expandedstorage:waxed_copper_barrel",
                "expandedstorage:waxed_exposed_copper_barrel",
                "expandedstorage:waxed_weathered_copper_barrel",
                "expandedstorage:waxed_oxidized_copper_barrel",
                "expandedstorage:iron_barrel",
                "expandedstorage:gold_barrel",
                "expandedstorage:diamond_barrel",
                "expandedstorage:obsidian_barrel",
                "expandedstorage:netherite_barrel",
                "expandedstorage:vanilla_wood_mini_chest",
                "expandedstorage:wood_mini_chest",
                "expandedstorage:pumpkin_mini_chest",
                "expandedstorage:red_mini_present",
                "expandedstorage:white_mini_present",
                "expandedstorage:candy_cane_mini_present",
                "expandedstorage:green_mini_present",
                "expandedstorage:lavender_mini_present",
                "expandedstorage:pink_amethyst_mini_present",
                "expandedstorage:iron_mini_chest",
                "expandedstorage:gold_mini_chest",
                "expandedstorage:diamond_mini_chest",
                "expandedstorage:obsidian_mini_chest",
                "expandedstorage:netherite_mini_chest",
                "expandedstorage:copper_mini_barrel",
                "expandedstorage:exposed_copper_mini_barrel",
                "expandedstorage:weathered_copper_mini_barrel",
                "expandedstorage:oxidized_copper_mini_barrel",
                "expandedstorage:waxed_copper_mini_barrel",
                "expandedstorage:waxed_exposed_copper_mini_barrel",
                "expandedstorage:waxed_weathered_copper_mini_barrel",
                "expandedstorage:waxed_oxidized_copper_mini_barrel",
                "expandedstorage:mini_barrel",
                "expandedstorage:iron_mini_barrel",
                "expandedstorage:gold_mini_barrel",
                "expandedstorage:diamond_mini_barrel",
                "expandedstorage:obsidian_mini_barrel",
                "expandedstorage:netherite_mini_barrel",
                "compact_storage:compact_chest_white",
                "compact_storage:compact_chest_orange",
                "compact_storage:compact_chest_magenta",
                "compact_storage:compact_chest_light_blue",
                "compact_storage:compact_chest_yellow",
                "compact_storage:compact_chest_lime",
                "compact_storage:compact_chest_pink",
                "compact_storage:compact_chest_gray",
                "compact_storage:compact_chest_light_gray",
                "compact_storage:compact_chest_cyan",
                "compact_storage:compact_chest_purple",
                "compact_storage:compact_chest_blue",
                "compact_storage:compact_chest_brown",
                "compact_storage:compact_chest_green",
                "compact_storage:compact_chest_red",
                "compact_storage:compact_chest_black",
                "compact_storage:compact_barrel_white",
                "compact_storage:compact_barrel_orange",
                "compact_storage:compact_barrel_magenta",
                "compact_storage:compact_barrel_light_blue",
                "compact_storage:compact_barrel_yellow",
                "compact_storage:compact_barrel_lime",
                "compact_storage:compact_barrel_pink",
                "compact_storage:compact_barrel_gray",
                "compact_storage:compact_barrel_light_gray",
                "compact_storage:compact_barrel_cyan",
                "compact_storage:compact_barrel_purple",
                "compact_storage:compact_barrel_blue",
                "compact_storage:compact_barrel_brown",
                "compact_storage:compact_barrel_green",
                "compact_storage:compact_barrel_red",
                "compact_storage:compact_barrel_black",
                "betterend:tenanea_chest",
                "betterend:mossy_glowshroom_chest",
                "betterend:pythadendron_chest",
                "betterend:end_lotus_chest",
                "betterend:lacugrove_chest",
                "betterend:dragon_tree_chest",
                "betterend:helix_tree_chest",
                "betterend:umbrella_tree_chest",
                "betterend:jellyshroom_chest",
                "betternether:rubeus_chest",
                "betterend:lucernia_chest",
                "betternether:nether_reed_chest",
                "betternether:stalagnate_chest",
                "betternether:willow_chest",
                "betternether:wart_chest",
                "betternether:chest_of_drawers",
                "betternether:crimson_chest",
                "betternether:warped_chest",
                "betternether:mushroom_fir_chest",
                "betternether:nether_mushroom_chest",
                "betternether:anchor_tree_chest",
                "betternether:nether_sakura_chest",
                "ironchests:diamond_chest",
                "ironchests:iron_chest",
                "ironchests:gold_chest",
                "ironchests:crystal_chest",
                "ironchests:copper_chest",
                "ironchests:dirt_chest",
                "ironchests:obsidian_chest",
                "ironchests:netherite_chest",
                "ironchests:iron_barrel",
                "ironchests:gold_barrel",
                "ironchests:crystal_barrel",
                "ironchests:copper_barrel",
                "ironchests:obsidian_barrel",
                "ironchests:netherite_barrel",
                "reinfchest:copper_chest",
                "reinfchest:iron_chest",
                "reinfchest:gold_chest",
                "reinfchest:diamond_chest",
                "reinfchest:netherite_chest",
                "reinfbarrel:copper_barrel",
                "reinfbarrel:iron_barrel",
                "reinfbarrel:gold_barrel",
                "reinfbarrel:diamond_barrel",
                "reinfbarrel:netherite_barrel",
                "reinfshulker:copper_shulker_box",
                "reinfshulker:white_copper_shulker_box",
                "reinfshulker:orange_copper_shulker_box",
                "reinfshulker:magenta_copper_shulker_box",
                "reinfshulker:light_blue_copper_shulker_box",
                "reinfshulker:yellow_copper_shulker_box",
                "reinfshulker:lime_copper_shulker_box",
                "reinfshulker:pink_copper_shulker_box",
                "reinfshulker:gray_copper_shulker_box",
                "reinfshulker:light_gray_copper_shulker_box",
                "reinfshulker:cyan_copper_shulker_box",
                "reinfshulker:purple_copper_shulker_box",
                "reinfshulker:blue_copper_shulker_box",
                "reinfshulker:brown_copper_shulker_box",
                "reinfshulker:green_copper_shulker_box",
                "reinfshulker:red_copper_shulker_box",
                "reinfshulker:black_copper_shulker_box",
                "reinfshulker:iron_shulker_box",
                "reinfshulker:white_iron_shulker_box",
                "reinfshulker:orange_iron_shulker_box",
                "reinfshulker:magenta_iron_shulker_box",
                "reinfshulker:light_blue_iron_shulker_box",
                "reinfshulker:yellow_iron_shulker_box",
                "reinfshulker:lime_iron_shulker_box",
                "reinfshulker:pink_iron_shulker_box",
                "reinfshulker:gray_iron_shulker_box",
                "reinfshulker:light_gray_iron_shulker_box",
                "reinfshulker:cyan_iron_shulker_box",
                "reinfshulker:purple_iron_shulker_box",
                "reinfshulker:blue_iron_shulker_box",
                "reinfshulker:brown_iron_shulker_box",
                "reinfshulker:green_iron_shulker_box",
                "reinfshulker:red_iron_shulker_box",
                "reinfshulker:black_iron_shulker_box",
                "reinfshulker:gold_shulker_box",
                "reinfshulker:white_gold_shulker_box",
                "reinfshulker:orange_gold_shulker_box",
                "reinfshulker:magenta_gold_shulker_box",
                "reinfshulker:light_blue_gold_shulker_box",
                "reinfshulker:yellow_gold_shulker_box",
                "reinfshulker:lime_gold_shulker_box",
                "reinfshulker:pink_gold_shulker_box",
                "reinfshulker:gray_gold_shulker_box",
                "reinfshulker:light_gray_gold_shulker_box",
                "reinfshulker:cyan_gold_shulker_box",
                "reinfshulker:purple_gold_shulker_box",
                "reinfshulker:blue_gold_shulker_box",
                "reinfshulker:brown_gold_shulker_box",
                "reinfshulker:green_gold_shulker_box",
                "reinfshulker:red_gold_shulker_box",
                "reinfshulker:black_gold_shulker_box",
                "reinfshulker:diamond_shulker_box",
                "reinfshulker:white_diamond_shulker_box",
                "reinfshulker:orange_diamond_shulker_box",
                "reinfshulker:magenta_diamond_shulker_box",
                "reinfshulker:light_blue_diamond_shulker_box",
                "reinfshulker:yellow_diamond_shulker_box",
                "reinfshulker:lime_diamond_shulker_box",
                "reinfshulker:pink_diamond_shulker_box",
                "reinfshulker:gray_diamond_shulker_box",
                "reinfshulker:light_gray_diamond_shulker_box",
                "reinfshulker:cyan_diamond_shulker_box",
                "reinfshulker:purple_diamond_shulker_box",
                "reinfshulker:blue_diamond_shulker_box",
                "reinfshulker:brown_diamond_shulker_box",
                "reinfshulker:green_diamond_shulker_box",
                "reinfshulker:red_diamond_shulker_box",
                "reinfshulker:black_diamond_shulker_box",
                "reinfshulker:netherite_shulker_box",
                "reinfshulker:white_netherite_shulker_box",
                "reinfshulker:orange_netherite_shulker_box",
                "reinfshulker:magenta_netherite_shulker_box",
                "reinfshulker:light_blue_netherite_shulker_box",
                "reinfshulker:yellow_netherite_shulker_box",
                "reinfshulker:lime_netherite_shulker_box",
                "reinfshulker:pink_netherite_shulker_box",
                "reinfshulker:gray_netherite_shulker_box",
                "reinfshulker:light_gray_netherite_shulker_box",
                "reinfshulker:cyan_netherite_shulker_box",
                "reinfshulker:purple_netherite_shulker_box",
                "reinfshulker:blue_netherite_shulker_box",
                "reinfshulker:brown_netherite_shulker_box",
                "reinfshulker:green_netherite_shulker_box",
                "reinfshulker:red_netherite_shulker_box",
                "reinfshulker:black_netherite_shulker_box",
                "sophisticatedstorage:chest",
                "sophisticatedstorage:iron_chest",
                "sophisticatedstorage:gold_chest",
                "sophisticatedstorage:diamond_chest",
                "sophisticatedstorage:netherite_chest",
                "sophisticatedstorage:barrel",
                "sophisticatedstorage:iron_barrel",
                "sophisticatedstorage:gold_barrel",
                "sophisticatedstorage:diamond_barrel",
                "sophisticatedstorage:netherite_barrel",
                "sophisticatedstorage:shulker_box",
                "sophisticatedstorage:iron_shulker_box",
                "sophisticatedstorage:gold_shulker_box",
                "sophisticatedstorage:diamond_shulker_box",
                "sophisticatedstorage:netherite_shulker_box");
        public Set<String> restockingSourceEntities = Set.of("minecraft:chest_boat",
                "minecraft:trader_llama",
                "minecraft:chest_minecart",
                "minecraft:donkey",
                "minecraft:llama",
                "minecraft:mule",
                "expandedstorage:wood_chest_minecart",
                "expandedstorage:pumpkin_chest_minecart",
                "expandedstorage:present_minecart",
                "expandedstorage:bamboo_chest_minecart",
                "expandedstorage:moss_chest_minecart",
                "expandedstorage:iron_chest_minecart",
                "expandedstorage:gold_chest_minecart",
                "expandedstorage:diamond_chest_minecart",
                "expandedstorage:obsidian_chest_minecart",
                "expandedstorage:netherite_chest_minecart");
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
        public KeySequence quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey = KeySequence.empty();
        public KeySequence restockFromNearbyContainersKey = KeySequence.empty();
        public KeySequence quickStackKey = KeySequence.empty();
        public KeySequence restockKey = KeySequence.empty();
        public KeySequence showMarkersForFavoritedItemsKey = new KeySequence(List.of(GLFW.GLFW_KEY_LEFT_ALT));
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
