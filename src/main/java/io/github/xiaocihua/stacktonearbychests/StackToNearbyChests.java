package io.github.xiaocihua.stacktonearbychests;

import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;
import io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget;
import io.github.xiaocihua.stacktonearbychests.mixin.AbstractContainerScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.HorseInventoryScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.RecipeBookComponentAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static java.util.function.Predicate.not;

@Environment(EnvType.CLIENT)
public class StackToNearbyChests implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("StackToNearbyChests");

    public static final boolean IS_IPN_MOD_LOADED = FabricLoader.getInstance().isModLoaded("inventoryprofilesnext");
    public static final boolean IS_EASY_SHULKER_BOXES_MOD_LOADED = FabricLoader.getInstance().isModLoaded("easyshulkerboxes");

    private static final ResourceLocation BUTTON_TEXTURES = ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "widget/");

    public static Optional<PosUpdatableButtonWidget> currentStackToNearbyContainersButton = Optional.empty();

    @Override
    public void onInitializeClient() {
        KeySequence.init();
        LockedSlots.init();
        InventoryActions.init();
        EndWorldTickExecutor.init();
        ForEachContainerTask.init();

        ScreenEvents.AFTER_INIT.register(this::addButtonsAndKeys);

        ModOptions.get().keymap.stackToNearbyContainersKey.registerNotOnScreen(InventoryActions::stackToNearbyContainers, InteractionResult.PASS);
        ModOptions.get().keymap.restockFromNearbyContainersKey.registerNotOnScreen(InventoryActions::restockFromNearbyContainers, InteractionResult.PASS);

        ModOptions.get().keymap.openModOptionsScreenKey.registerNotOnScreen(
                () -> Minecraft.getInstance().setScreen(new ModOptionsScreen(new ModOptionsGui())), InteractionResult.FAIL);
    }

    private void addButtonsAndKeys(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        ModOptions.Appearance appearanceOption = ModOptions.get().appearance;
        boolean showButtonTooltip = appearanceOption.showButtonTooltip.booleanValue();

        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {

            if (appearanceOption.showTheButtonsOnTheCreativeInventoryScreen.booleanValue()) {
                addButtonsOnInventoryScreen((AbstractContainerScreen<?>) screen, showButtonTooltip, appearanceOption);
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, context) -> {
                if (isTextFieldActive(scr) || isInventoryTabNotSelected(scr)) {
                    return;
                }

                ModOptions.Keymap keymap = ModOptions.get().keymap;

                boolean triggered = false;

                Slot focusedSlot = ((AbstractContainerScreenAccessor) screen).getFocusedSlot();
                if (focusedSlot != null && focusedSlot.hasItem()) {
                    triggered = keymap.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey
                            .testThenRun(() -> InventoryActions.stackToNearbyContainers(focusedSlot.getItem().getItem()));
                }

                if (!triggered) {
                    keymap.stackToNearbyContainersKey.testThenRun(InventoryActions::stackToNearbyContainers);
                }

                keymap.restockFromNearbyContainersKey.testThenRun(InventoryActions::restockFromNearbyContainers);
            });
        } else if (isContainerScreen(screen)) {
            AbstractContainerMenu screenHandler = ((AbstractContainerScreen<?>) screen).getMenu();

            if (ModOptions.get().appearance.showQuickStackButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonTextures("quick_stack_button"))
                        .setTooltip(showButtonTooltip ? Component.translatable("stack-to-nearby-chests.tooltip.quickStackButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.quickStackButtonPosX, appearanceOption.quickStackButtonPosY))
                        .setPressAction(button -> InventoryActions.quickStack(screenHandler))
                        .build();
            }

            if (ModOptions.get().appearance.showRestockButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonTextures("restock_button"))
                        .setTooltip(showButtonTooltip ? Component.translatable("stack-to-nearby-chests.tooltip.restockButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.restockButtonPosX, appearanceOption.restockButtonPosY))
                        .setPressAction(button -> InventoryActions.restock(screenHandler))
                        .build();
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, context) -> {
                if (isTextFieldActive(scr) || isInventoryTabNotSelected(scr)) {
                    return;
                }

                ModOptions.get().keymap.quickStackKey.testThenRun(() -> InventoryActions.quickStack(screenHandler));
                ModOptions.get().keymap.restockKey.testThenRun(() -> InventoryActions.restock(screenHandler));
            });
        }
    }

    private static void addButtonsOnInventoryScreen(AbstractContainerScreen<?> screen, boolean showButtonTooltip, ModOptions.Appearance appearanceOption) {
        if (ModOptions.get().appearance.showStackToNearbyContainersButton.booleanValue()) {
            var buttonWidget = new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonTextures("quick_stack_to_nearby_containers_button"))
                    .setTooltip(showButtonTooltip ? getTooltipWithHint("stack-to-nearby-chests.tooltip.stackToNearbyContainersButton") : null)
                    .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                            parent.getY() + appearanceOption.stackToNearbyContainersButtonPosY.intValue()))
                    .setPressAction(button -> {
                        AbstractContainerMenu screenHandler = screen.getMenu();
                        ItemStack cursorStack = screenHandler.getCarried();
                        if (cursorStack.isEmpty()) {
                            InventoryActions.stackToNearbyContainers();
                        } else {
                            Item item = cursorStack.getItem();

                            screenHandler.slots.stream()
                                    .filter(slot -> slot.container instanceof Inventory)
                                    .filter(slot -> slot.getContainerSlot() < 36) // 36: feet, 37: legs, 38: chest, 39: head, 40: offhand
                                    .filter(not(LockedSlots::isLocked))
                                    .filter(slot -> !slot.hasItem() || InventoryActions.canMerge(slot.getItem(), cursorStack))
                                    .peek(slot -> InventoryActions.pickup(screenHandler, slot))
                                    .anyMatch(slot -> cursorStack.isEmpty());

                            InventoryActions.stackToNearbyContainers(item);
                        }
                    })
                    .build();

            currentStackToNearbyContainersButton = Optional.ofNullable(buttonWidget);

            ((ScreenExtensions) screen).fabric_getRemoveEvent().register(s -> currentStackToNearbyContainersButton = Optional.empty());
        }

        if (ModOptions.get().appearance.showRestockFromNearbyContainersButton.booleanValue()) {
            new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonTextures("restock_from_nearby_containers_button"))
                    .setTooltip(showButtonTooltip ? getTooltipWithHint("stack-to-nearby-chests.tooltip.restockFromNearbyContainersButton") : null)
                    .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                            parent.getY() + appearanceOption.restockFromNearbyContainersButtonPosY.intValue()))
                    .setPressAction(button -> InventoryActions.restockFromNearbyContainers())
                    .build();
        }
    }

    private static WidgetSprites getButtonTextures(String name) {
        return new WidgetSprites(BUTTON_TEXTURES.withSuffix(name), BUTTON_TEXTURES.withSuffix(name + "_highlighted"));
    }

    private static boolean isTextFieldActive(Screen screen) {
        GuiEventListener focusedElement = screen.getFocused();

        if (focusedElement instanceof RecipeBookComponent) {
            EditBox searchField = ((RecipeBookComponentAccessor) focusedElement).getSearchBox();
            if (searchField != null && searchField.canConsumeInput()) {
                return true;
            }
        }

        return focusedElement instanceof EditBox textField && textField.canConsumeInput();
    }

    private static boolean isInventoryTabNotSelected(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen creativeInventoryScreen
                && !creativeInventoryScreen.isInventoryOpen();
    }

    public static Vec2i getAbsolutePos(AbstractContainerScreenAccessor parent, ModOptions.IntOption x, ModOptions.IntOption y) {
        return new Vec2i(parent.getX() + parent.getImageWidth() + x.intValue(),
                parent.getY() + parent.getImageHeight() / 2 + y.intValue());
    }

    private static Component getTooltipWithHint(String translationKey) {
        return Component.translatable(translationKey)
                .append("\n")
                .append(Component.translatable("stack-to-nearby-chests.tooltip.hint").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_GRAY)));
    }

    public static boolean isContainerScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) {
            return false;
        } else if (
                screen instanceof BeaconScreen
                        || screen instanceof GrindstoneScreen
                        || screen instanceof CartographyTableScreen
                        || screen instanceof CraftingScreen
                        || screen instanceof LoomScreen
                        || screen instanceof EnchantmentScreen
                        || screen instanceof MerchantScreen
                        || screen instanceof ItemCombinerScreen<?>
                        || screen instanceof StonecutterScreen
                        || screen instanceof InventoryScreen
                        || screen instanceof CreativeModeInventoryScreen
        ) {
            return false;
        } else if (screen instanceof HorseInventoryScreen) {
            return ((HorseInventoryScreenAccessor) screen).getHorse() instanceof AbstractChestedHorse abstractDonkeyEntity
                    && abstractDonkeyEntity.hasChest();
        }

        return true;
    }
}