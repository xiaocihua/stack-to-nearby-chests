package io.github.xiaocihua.stacktonearbychests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.xiaocihua.stacktonearbychests.event.ClickSlotCallback;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.MinecraftServerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static java.util.function.Predicate.not;

// Locked slots contain favorite item stacks
@Environment(EnvType.CLIENT)
public class LockedSlots {
    private static final Path LOCKED_SLOTS_FOLDER = ModOptions.MOD_OPTIONS_DIR.resolve("locked-slots");
    public static final List<Identifier> FAVORITE_ITEM_TAGS = List.of(new Identifier(ModOptions.MOD_ID, "gold_badge"),
            new Identifier(ModOptions.MOD_ID, "red_background"),
            new Identifier(ModOptions.MOD_ID, "gold_border"),
            new Identifier(ModOptions.MOD_ID, "iron_border"));

    private static HashSet<Integer> currentLockedSlots = new HashSet<>();
    private static boolean isMovingFavoriteItemStack = false;
    private static Slot quickMoveDestination;
    @Nullable
    private static SlotActionType actionBeingExecuted;

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> read(client));

        ModOptions.get().keymap.markAsFavoriteKey.registerOnScreen(HandledScreen.class, screen -> {
            MinecraftClient client = MinecraftClient.getInstance();
            double x = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            double y = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
            Slot slot = ((HandledScreenAccessor) screen).invokeGetSlotAt(x, y);
            if (!unLock(slot) && slot != null && slot.hasStack()) {
                lock(slot);
            }
        }, ActionResult.FAIL);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handledScreen) {
                if (!isEnabled()) {
                    currentLockedSlots.clear();
                }
                refresh(handledScreen.getScreenHandler());
                ScreenEvents.remove(screen).register(s -> isMovingFavoriteItemStack = false);
            }
        });

        // Before disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            write(client);
            currentLockedSlots.clear();
        });

        ClickSlotCallback.BEFORE.register((syncId, slotId, button, actionType, player) -> {
            @Nullable
            Slot slot = slotId < 0 ? null : player.currentScreenHandler.getSlot(slotId);
            if (isLocked(slot) && (
                    actionType == SlotActionType.PICKUP && ModOptions.get().behavior.favoriteItemsCannotBePickedUp.booleanValue()
                            || actionType == SlotActionType.QUICK_MOVE && ModOptions.get().behavior.favoriteItemStacksCannotBeQuickMoved.booleanValue()
                            || actionType == SlotActionType.SWAP && ModOptions.get().behavior.favoriteItemStacksCannotBeSwapped.booleanValue()
                            || actionType == SlotActionType.THROW && ModOptions.get().behavior.favoriteItemStacksCannotBeThrown.booleanValue()
            )) {
                return ActionResult.FAIL;
            }

            actionBeingExecuted = actionType;

            return ActionResult.PASS;
        });

        ClickSlotCallback.AFTER.register((syncId, slotId, button, actionType, player) -> {
            afterClickSlot(slotId, button, actionType, player);
            return ActionResult.PASS;
        });
    }

    private static void read(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        Path path = getLockedSlotsFilePath(client);
        if (path == null) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<HashSet<Integer>>() {}.getType();
            currentLockedSlots = new Gson().fromJson(reader, type);
        } catch (IOException e) {
            StackToNearbyChests.LOGGER.info("Locked slots file does not exist", e);
        }
    }

    private static void write(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        Path path = getLockedSlotsFilePath(client);
        if (path == null) {
            return;
        }

        try {
            Files.createDirectories(LOCKED_SLOTS_FOLDER);
            String json = new Gson().toJson(currentLockedSlots);
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            StackToNearbyChests.LOGGER.error("Failed to write locked slots file", e);
        }
    }

    @Nullable
    private static Path getLockedSlotsFilePath(MinecraftClient client) {
        IntegratedServer integratedServer = client.getServer();
        ServerInfo currentServerEntry = client.getCurrentServerEntry();
        String fileName;
        if (integratedServer != null) {
            fileName = ((MinecraftServerAccessor) integratedServer).getSession().getDirectoryName().concat(".json");
        } else if (currentServerEntry != null){
            fileName = currentServerEntry.address.concat(".json").replace(":", "colon");
        } else {
            StackToNearbyChests.LOGGER.info("Could not get level name or server address");
            return null;
        }

        return LOCKED_SLOTS_FOLDER.resolve(fileName);
    }

    private static boolean isEnabled() {
        return ModOptions.get().behavior.enableItemFavoriting.booleanValue();
    }

    private static boolean lock(@Nullable Slot slot) {
        return isLockable(slot) && lock(slot.getIndex());
    }

    private static boolean lock(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.add(slotIndex);
    }

    private static boolean unLock(@Nullable Slot slot) {
        return isLockable(slot) && unLock(slot.getIndex());
    }

    private static boolean unLock(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.remove(slotIndex);
    }

    private static boolean setLocked(int slotIndex, boolean locked) {
        return locked ? lock(slotIndex) : unLock(slotIndex);
    }

    public static boolean isLocked(@Nullable Slot slot) {
        return isLockable(slot) && isLocked(slot.getIndex());
    }

    public static boolean isLocked(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.contains(slotIndex);
    }

    private static boolean isLockable(@Nullable Slot slot) {
        return isEnabled()
                && slot != null
                && slot.inventory instanceof PlayerInventory
                && !MinecraftClient.getInstance().player.getAbilities().creativeMode;
    }

    private static boolean isLockable(int slotIndex) {
        return isEnabled()
                && slotIndex >= 0
                && slotIndex != 39 // Head
                && slotIndex != 38 // Chest
                && slotIndex != 37 // Legs
                && slotIndex != 36;// Feet
    }

    public static void onSetStack(int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            if (actionBeingExecuted == null || actionBeingExecuted == SlotActionType.THROW) {
                unLock(slotIndex);
            } else if (actionBeingExecuted == SlotActionType.PICKUP_ALL) {
                if (unLock(slotIndex)) {
                    isMovingFavoriteItemStack = true;
                }
            }
        }
    }

    public static void onInsertItem(Slot destination) {
        quickMoveDestination = destination;
    }

    private static void afterClickSlot(int slotId, int button, SlotActionType actionType, PlayerEntity player) {
        ScreenHandler screenHandler = player.currentScreenHandler;
        @Nullable
        Slot slot = slotId < 0 ? null : screenHandler.getSlot(slotId);
        switch (actionType) {
            case PICKUP -> {
                if (slotId == ScreenHandler.EMPTY_SPACE_SLOT_INDEX) { // Throw
                    isMovingFavoriteItemStack = false;
                }
                if (slot == null) {
                    break;
                }

                ItemStack cursorStack = screenHandler.getCursorStack();
                ItemStack slotStack = slot.getStack();
                if (isMovingFavoriteItemStack) {
                    if (cursorStack.isEmpty()) {
                        lock(slot);
                        isMovingFavoriteItemStack = false;
                    } else if (!ItemStack.canCombine(cursorStack, slotStack)) { // Swap the slot with the cursor
                        if (!isLocked(slot)) {
                            isMovingFavoriteItemStack = false;
                        }
                        lock(slot);
                    }
                } else {
                    if (isLocked(slot) && slotStack.isEmpty()) {
                        unLock(slot);
                        isMovingFavoriteItemStack = true;
                    } else if (!ItemStack.canCombine(cursorStack, slotStack)) { // Swap the slot with the cursor
                        if (isLocked(slot)) {
                            isMovingFavoriteItemStack = true;
                        }
                        unLock(slot);
                    }
                }
            }
            case QUICK_MOVE -> {
                if (slot == null) {
                    break;
                }
                if (isLocked(slot) && !slot.hasStack()) {
                    unLock(slot);
                    lock(quickMoveDestination);
                }
            }
            case SWAP -> {
                boolean isFromSlotLocked = unLock(slot);
                boolean isToSlotLocked = unLock(button); // The variable button holds the index of hotbar slot when swapping
                if (isFromSlotLocked) {
                    lock(button);
                }
                if (isToSlotLocked) {
                    lock(slot);
                }
            }
            case QUICK_CRAFT -> {
                if (screenHandler.getCursorStack().isEmpty()) {
                    isMovingFavoriteItemStack = false;
                } else if (isMovingFavoriteItemStack) {
                    lock(slot);
                }
            }
        }

        actionBeingExecuted = null;
    }

    /**
     * Unfavorite all empty slots.
     * @return If any slots have been unmarked as favorites.
     */
    private static boolean refresh(ScreenHandler screenHandler) {
        return screenHandler.slots.stream()
                .filter(not(Slot::hasStack))
                .map(LockedSlots::unLock)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    public static void onSetGameMode(GameMode gameMode) {
        if (gameMode == GameMode.CREATIVE) {
            currentLockedSlots.clear();
        }
    }

    public static void drawFavoriteItemStyle(MatrixStack matrices, Slot slot, boolean isForeground) {
        Identifier id = ModOptions.get().appearance.favoriteItemStyle;
        if (isLocked(slot) && isForeground == id.getPath().equals("gold_badge")) {
            Sprite sprite = MinecraftClient.getInstance()
                    .getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                    .apply(new Identifier(id.getNamespace(), "item/" + id.getPath()));
            RenderSystem.setShaderTexture(0, sprite.getAtlasId());
            DrawableHelper.drawSprite(matrices, slot.x, slot.y, isForeground ? 300 : 200, 16, 16, sprite);
        }
    }

    public static ActionResult beforeDropSelectedItem(int selectedSlotIndex) {
        if (isLocked(selectedSlotIndex) && ModOptions.get().behavior.favoriteItemStacksCannotBeThrown.booleanValue()) {
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    public static void afterDropSelectedItem(int selectedSlotIndex) {
        if (isLocked(selectedSlotIndex)
                && !MinecraftClient.getInstance().player.playerScreenHandler.slots.get(selectedSlotIndex).hasStack()) {
            unLock(selectedSlotIndex);
        }
    }

    public static ActionResult onSwapItemWithOffhand() {
        int selectedSlotIndex = MinecraftClient.getInstance().player.getInventory().selectedSlot;
        boolean isSelectedSlotLocked = isLocked(selectedSlotIndex);

        if (isSelectedSlotLocked && ModOptions.get().behavior.favoriteItemsCannotBeSwappedWithOffhand.booleanValue()) {
            return ActionResult.FAIL;
        }

        setLocked(selectedSlotIndex, isLocked(PlayerInventory.OFF_HAND_SLOT));
        setLocked(PlayerInventory.OFF_HAND_SLOT, isSelectedSlotLocked);

        return ActionResult.PASS;
    }
}
