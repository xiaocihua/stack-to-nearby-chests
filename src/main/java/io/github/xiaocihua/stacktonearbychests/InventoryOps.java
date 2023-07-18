package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import io.github.xiaocihua.stacktonearbychests.event.SetScreenCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toSet;

public class InventoryOps {
    private static final BlockingQueue<ScreenHandler> REQUEST_QUEUE = new ArrayBlockingQueue<>(1);
    @Nullable
    private static Thread forEachContainerThread;

    public static void init() {
        SetScreenCallback.EVENT.register(screen -> {
            if (!isTerminated()) {
                if (screen instanceof DeathScreen) {
                    interruptCurrentOperation();
                    return ActionResult.PASS;
                }

                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        OnKeyCallback.PRESS.register(key -> {
            if (isRunning()){
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    interruptCurrentOperation();
                }

                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    public static void onUpdateSlotStacks(ScreenHandler handler) {
        if (isRunning()
                && !"com.github.clevernucleus.playerex.factory.ExScreenFactory.Handler".equals(handler.getClass().getCanonicalName())
        ) {
            REQUEST_QUEUE.add(handler);
        }
    }

    public static boolean isRunning() {
        return forEachContainerThread != null && !forEachContainerThread.isInterrupted();
    }

    public static boolean isTerminated() {
        return forEachContainerThread == null;
    }

    public static void interruptCurrentOperation() {
        if (forEachContainerThread != null) {
            forEachContainerThread.interrupt();
        }
    }

    public static void stackToNearbyContainers() {
        forEachContainer(InventoryOps::quickStack, ModOptions.get().behavior.stackingTargets);
    }

    public static void restockFromNearbyContainers() {
        forEachContainer(InventoryOps::restock, ModOptions.get().behavior.restockingSources);
    }

    public static void forEachContainer(Consumer<ScreenHandler> action, Collection<String> filter) {
        Runnable task = () -> {
            try {
                forEachContainerInternal(action, filter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendChatMessage("stack-to-nearby-chests.message.operationInterrupted");
            } catch (TimeoutException e) {
                sendChatMessage("stack-to-nearby-chests.message.interruptedByTimeout");
            } catch (Exception e) {
                sendChatMessage("stack-to-nearby-chests.message.exceptionOccurred");
                StackToNearbyChests.LOGGER.error("An exception occurred during the operation", e);
            } finally {
                REQUEST_QUEUE.clear();
                MinecraftClient.getInstance().executeSync(() -> {
                    MinecraftClient.getInstance().player.closeHandledScreen();
                    forEachContainerThread = null;
                });
            }
        };
        forEachContainerThread = new Thread(task, "For Each Container");
        forEachContainerThread.start();
    }

    /**
     * Adapted from Earthcomputer's <a href = "https://github.com/Earthcomputer/clientcommands">ClientCommands</a>.
     */
    private static void forEachContainerInternal(Consumer<ScreenHandler> action, Collection<String> filter) throws InterruptedException, TimeoutException {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if (player.isSneaking() || player.isSpectator()) {
            return;
        }
        ClientPlayerInteractionManager interactionManager = client.interactionManager;

        Vec3d origin = cameraEntity.getCameraPosVec(0);
        float reachDistance = interactionManager.getReachDistance();
        Box box = MathUtil.getBox(origin, reachDistance);
        float squaredReachDistance = reachDistance * reachDistance;

        Set<BlockPos> searchedBlocks = new HashSet<>();
        boolean hasSearchedEnderChest = false;

        for (BlockPos pos : MathUtil.getBlocksInBox(box)) {
            if (!canSearch(world, pos)) {
                continue;
            }
            if (searchedBlocks.contains(pos)) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            if (!filter.contains(Registry.BLOCK.getId(state.getBlock()).toString())) {
                continue;
            }
            Vec3d closestPos = MathUtil.getClosestPoint(pos, state.getOutlineShape(world, pos), origin);
            if (closestPos.squaredDistanceTo(origin) > squaredReachDistance) {
                continue;
            }

            BlockPos immutablePos = pos.toImmutable();
            searchedBlocks.add(immutablePos);
            if (state.getBlock() == Blocks.ENDER_CHEST) {
                if (hasSearchedEnderChest) {
                    continue;
                }
                hasSearchedEnderChest = true;
            } else {
                getTheOtherHalfOfLargeChest(world, pos).ifPresent(searchedBlocks::add);
            }

            interactionManager.interactBlock(player, world, Hand.MAIN_HAND,
                    new BlockHitResult(closestPos,
                            MathUtil.getFacingDirection(closestPos.subtract(origin)),
                            immutablePos,
                            false));

            action.accept(getRequestedScreenHandler());

            Thread.sleep(ModOptions.get().behavior.searchInterval.intValue());
        }
    }

    private static ScreenHandler getRequestedScreenHandler() throws InterruptedException, TimeoutException {
        ScreenHandler screenHandler = REQUEST_QUEUE.poll(4, TimeUnit.SECONDS);
        if (screenHandler == null) {
            throw new TimeoutException();
        }
        return screenHandler;
    }

    /**
     * Adapted from Earthcomputer's <a href = "https://github.com/Earthcomputer/clientcommands">ClientCommands</a>.
     */
    private static boolean canSearch(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof Inventory) && state.getBlock() != Blocks.ENDER_CHEST) {
            return false;
        }
        if (state.getBlock() instanceof ChestBlock || state.getBlock() == Blocks.ENDER_CHEST) {
            if (ChestBlock.isChestBlocked(world, pos)){
                return false;
            }
            return getTheOtherHalfOfLargeChest(world, pos)
                    .map(offsetPos -> !ChestBlock.isChestBlocked(world, offsetPos))
                    .orElse(true);
        }
        return true;
    }

    private static Optional<BlockPos> getTheOtherHalfOfLargeChest(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            BlockPos offsetPos = pos.offset(ChestBlock.getFacing(state)); // getFacing(BlockState) returns the direction in which the other half of the chest is located
            BlockState theOtherHalf = world.getBlockState(offsetPos);
            if(theOtherHalf.getBlock() == state.getBlock()
                    && state.get(ChestBlock.FACING) == theOtherHalf.get(ChestBlock.FACING)
                    && ChestBlock.getFacing(state) == ChestBlock.getFacing(theOtherHalf).getOpposite()) {
                return Optional.ofNullable(offsetPos);
            }
        }
        return Optional.empty();
    }

    public static void quickStack(ScreenHandler screenHandler) {
        var slots = SlotsInScreenHandler.of(screenHandler);

        Set<Item> itemsInContainer = slots.containerSlots().stream()
                .map(slot -> slot.getStack().getItem())
                .filter(item -> !ModOptions.get().behavior.itemsThatWillNotBeStacked.contains(Registry.ITEM.getId(item).toString()))
                .collect(toSet());

        slots.playerSlots().stream()
                .filter(slot -> !(ModOptions.get().behavior.doNotQuickStackItemsFromTheHotbar.booleanValue()
                        && PlayerInventory.isValidHotbarIndex(slot.getIndex())))
                .filter(not(LockedSlots::isLocked))
                .filter(slot -> itemsInContainer.contains(slot.getStack().getItem()))
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

    private static void quickMove(ScreenHandler screenHandler, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.interactionManager.clickSlot(screenHandler.syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.QUICK_MOVE, client.player);
    }

    private static void pickup(ScreenHandler screenHandler, Slot slot) {
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

    private static void sendChatMessage(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new TranslatableText(message));
    }
}
