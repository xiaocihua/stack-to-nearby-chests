package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.ClickSlotCallback;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER;
import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.currentStackToNearbyContainersButton;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toSet;

public class InventoryActions {

    public static void init() {
        ClickSlotCallback.BEFORE.register((syncId, slotId, button, actionType, player) ->
        {
            if (slotId == -999
                    && actionType == ClickType.PICKUP
                    && currentStackToNearbyContainersButton.map(AbstractWidget::isHovered).orElse(false)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    public static void stackToNearbyContainers() {
        forEachContainer(InventoryActions::quickStack, ModOptions.get().behavior.stackingTargets, ModOptions.get().behavior.stackingTargetEntities);
    }

    public static void stackToNearbyContainers(Item item) {
        forEachContainer(screenHandler -> quickStack(screenHandler, item), ModOptions.get().behavior.stackingTargets, ModOptions.get().behavior.stackingTargetEntities);
    }

    public static void restockFromNearbyContainers() {
        forEachContainer(InventoryActions::restock, ModOptions.get().behavior.restockingSources, ModOptions.get().behavior.restockingSourceEntities);
    }

    public static boolean canMerge(ItemStack stack, ItemStack otherStack) {
        return stack.getCount() < stack.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack, otherStack);
    }

    public static void forEachContainer(Consumer<AbstractContainerMenu> action, Collection<String> blockFilter, Collection<String> entityFilter) {
        Minecraft client = Minecraft.getInstance();

        Entity cameraEntity = client.getCameraEntity();
        Level world = client.level;
        MultiPlayerGameMode interactionManager = client.gameMode;
        LocalPlayer player = client.player;
        if (cameraEntity == null || world == null || interactionManager == null || player == null) {
            LOGGER.info("cameraEntity: {}, word: {}, interactionManager: {}, player: {}", cameraEntity, world ,interactionManager, player);
            return;
        } else if (player.isSpectator()) {
            LOGGER.info("The player is in spectator mode");
            return;
        } else if (player.isShiftKeyDown()) {
            LOGGER.info("The player is sneaking");
            return;
        }

        var task = new ForEachBlockContainerTask(client, cameraEntity, world, player, interactionManager, action, blockFilter);

        if (ModOptions.get().behavior.supportForContainerEntities.booleanValue() && !player.isPassenger()) {
            task.thenStart(new ForEachEntityContainerTask(client, player, action, cameraEntity, world, interactionManager, entityFilter));
        }

        task.start();
    }

    public static void quickStack(AbstractContainerMenu screenHandler) {
        var slots = SlotsInScreenHandler.of(screenHandler);

        Set<Item> itemsInContainer = slots.containerSlots().stream()
                .map(slot -> slot.getItem().getItem())
                .filter(item -> !ModOptions.get().behavior.itemsThatWillNotBeStacked.contains(BuiltInRegistries.ITEM.getKey(item).toString()))
                .collect(toSet());

        moveAll(screenHandler, slots.playerSlots, itemsInContainer);
    }

    public static void quickStack(AbstractContainerMenu screenHandler, Item item) {
        var slots = SlotsInScreenHandler.of(screenHandler);

        boolean hasSameTypeItems = slots.containerSlots.stream()
                .anyMatch(slot -> slot.getItem().is(item));

        if (hasSameTypeItems) {
            moveAll(screenHandler, slots.playerSlots(), Set.of(item));
        }
    }

    private static void moveAll(AbstractContainerMenu screenHandler, List<Slot> playerSlots, Set<Item> itemsToBeMoved) {
        playerSlots.stream()
                .filter(slot -> !(ModOptions.get().behavior.doNotQuickStackItemsFromTheHotbar.booleanValue()
                        && Inventory.isHotbarSlot(slot.getContainerSlot())))
                .filter(not(InventoryActions::isSlotLocked))
                .filter(slot -> itemsToBeMoved.contains(slot.getItem().getItem()))
                .filter(slot -> slot.mayPickup(Minecraft.getInstance().player))
                .filter(Slot::hasItem)
                .forEach(slot -> quickMove(screenHandler, slot));
    }

    public static void restock(AbstractContainerMenu screenHandler) {
        var slots = SlotsInScreenHandler.of(screenHandler);
        slots.playerSlots().stream()
                .filter(Slot::hasItem)
                .filter(slot -> slot.getItem().isStackable())
                .filter(slot -> !ModOptions.get().behavior.itemsThatWillNotBeRestocked
                        .contains(BuiltInRegistries.ITEM
                                .getKey(slot.getItem().getItem())
                                .toString()))
                .forEach(slot -> slots.containerSlots().stream()
                        .filter(containerSlot -> ItemStack.isSameItemSameComponents(slot.getItem(), containerSlot.getItem()))
                        .peek(containerSlot -> {
                            pickup(screenHandler, containerSlot);
                            pickup(screenHandler, slot);
                        })
                        .filter(containerSlot -> !screenHandler.getCarried().isEmpty())
                        .findFirst()
                        .ifPresent(containerSlot -> pickup(screenHandler, containerSlot))
                );
    }

    public static void quickMove(AbstractContainerMenu screenHandler, Slot slot) {
        Minecraft client = Minecraft.getInstance();
        client.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot.index, GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, client.player);
    }

    public static void pickup(AbstractContainerMenu screenHandler, Slot slot) {
        Minecraft client = Minecraft.getInstance();
        client.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot.index, GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.PICKUP, client.player);
    }

    private record SlotsInScreenHandler(List<Slot> playerSlots, List<Slot> containerSlots) {

        static SlotsInScreenHandler of(AbstractContainerMenu screenHandler) {
            Map<Boolean, List<Slot>> inventories = screenHandler.slots.stream()
                    .collect(partitioningBy(slot -> slot.container instanceof Inventory));

            return new SlotsInScreenHandler(inventories.get(true), inventories.get(false));
        }
    }

    private static boolean isSlotLocked(Slot slot) {
        if (StackToNearbyChests.IS_IPN_MOD_LOADED) {
            try {
                Class<?> clazz = Class.forName("org.anti_ad.mc.ipnext.event.LockSlotsHandler");
                Object instance = clazz.getField("INSTANCE").get(null);
                Boolean slotLocked = (Boolean) clazz.getMethod("isMappedSlotLocked", Slot.class)
                        .invoke(instance, slot);
                if (slotLocked) {
                    return true;
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException | NoSuchFieldException e) {
                StackToNearbyChests.LOGGER.warn("An exception occurred when determining whether the slot is locked by IPN mod", e);
            }
        }

        return LockedSlots.isLocked(slot);
    }
}
