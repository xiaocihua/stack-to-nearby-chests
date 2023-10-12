package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.ClickSlotCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
                    && actionType == SlotActionType.PICKUP
                    && currentStackToNearbyContainersButton.map(ClickableWidget::isHovered).orElse(false)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
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
        return stack.getCount() < stack.getMaxCount() && ItemStack.canCombine(stack, otherStack);
    }

    public static void forEachContainer(Consumer<ScreenHandler> action, Collection<String> blockFilter, Collection<String> entityFilter) {
        MinecraftClient client = MinecraftClient.getInstance();

        Entity cameraEntity = client.getCameraEntity();
        World world = client.world;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientPlayerEntity player = client.player;
        if (cameraEntity == null || world == null || interactionManager == null || player == null) {
            LOGGER.info("cameraEntity: {}, word: {}, interactionManager: {}, player: {}", cameraEntity, world ,interactionManager, player);
            return;
        } else if (player.isSpectator()) {
            LOGGER.info("The player is in spectator mode");
            return;
        } else if (player.isSneaking()) {
            LOGGER.info("The player is sneaking");
            return;
        }

        var task = new ForEachBlockContainerTask(client, cameraEntity, world, player, interactionManager, action, blockFilter);

        if (ModOptions.get().behavior.supportForContainerEntities.booleanValue() && !player.hasVehicle()) {
            task.thenStart(new ForEachEntityContainerTask(client, player, action, cameraEntity, world, interactionManager, entityFilter));
        }

        task.start();
    }

    public static void quickStack(ScreenHandler screenHandler) {
        var slots = SlotsInScreenHandler.of(screenHandler);

        Set<Item> itemsInContainer = slots.containerSlots().stream()
                .map(slot -> slot.getStack().getItem())
                .filter(item -> !ModOptions.get().behavior.itemsThatWillNotBeStacked.contains(Registry.ITEM.getId(item).toString()))
                .collect(toSet());

        moveAll(screenHandler, slots.playerSlots, itemsInContainer);
    }

    public static void quickStack(ScreenHandler screenHandler, Item item) {
        var slots = SlotsInScreenHandler.of(screenHandler);

        boolean hasSameTypeItems = slots.containerSlots.stream()
                .anyMatch(slot -> slot.getStack().isOf(item));

        if (hasSameTypeItems) {
            moveAll(screenHandler, slots.playerSlots(), Set.of(item));
        }
    }

    private static void moveAll(ScreenHandler screenHandler, List<Slot> playerSlots, Set<Item> itemsToBeMoved) {
        playerSlots.stream()
                .filter(slot -> !(ModOptions.get().behavior.doNotQuickStackItemsFromTheHotbar.booleanValue()
                        && PlayerInventory.isValidHotbarIndex(slot.getIndex())))
                .filter(not(InventoryActions::isSlotLocked))
                .filter(slot -> itemsToBeMoved.contains(slot.getStack().getItem()))
                .filter(slot -> slot.canTakeItems(MinecraftClient.getInstance().player))
                .filter(Slot::hasStack)
                .forEach(slot -> quickMove(screenHandler, slot));
    }

    public static void restock(ScreenHandler screenHandler) {
        var slots = SlotsInScreenHandler.of(screenHandler);
        slots.playerSlots().stream()
                .filter(Slot::hasStack)
                .filter(slot -> slot.getStack().isStackable())
                .filter(slot -> !ModOptions.get().behavior.itemsThatWillNotBeRestocked
                        .contains(Registry.ITEM
                                .getId(slot.getStack().getItem())
                                .toString()))
                .forEach(slot -> slots.containerSlots().stream()
                        .filter(containerSlot -> ItemStack.canCombine(slot.getStack(), containerSlot.getStack()))
                        .peek(containerSlot -> {
                            pickup(screenHandler, containerSlot);
                            pickup(screenHandler, slot);
                        })
                        .filter(containerSlot -> !screenHandler.getCursorStack().isEmpty())
                        .findFirst()
                        .ifPresent(containerSlot -> pickup(screenHandler, containerSlot))
                );
    }

    public static void quickMove(ScreenHandler screenHandler, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.interactionManager.clickSlot(screenHandler.syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.QUICK_MOVE, client.player);
    }

    public static void pickup(ScreenHandler screenHandler, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.interactionManager.clickSlot(screenHandler.syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.PICKUP, client.player);
    }

    private record SlotsInScreenHandler(List<Slot> playerSlots, List<Slot> containerSlots) {

        static SlotsInScreenHandler of(ScreenHandler screenHandler) {
            Map<Boolean, List<Slot>> inventories = screenHandler.slots.stream()
                    .collect(partitioningBy(slot -> slot.inventory instanceof PlayerInventory));

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
