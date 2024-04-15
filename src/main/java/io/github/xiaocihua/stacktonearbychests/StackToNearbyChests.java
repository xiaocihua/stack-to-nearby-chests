package io.github.xiaocihua.stacktonearbychests;

import com.mojang.logging.LogUtils;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;
import io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.HorseScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.RecipeBookWidgetAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.Optional;

import static java.util.function.Predicate.not;

@Environment(EnvType.CLIENT)
public class StackToNearbyChests implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean IS_IPN_MOD_LOADED = FabricLoader.getInstance().isModLoaded("inventoryprofilesnext");
    public static final boolean IS_EASY_SHULKER_BOXES_MOD_LOADED = FabricLoader.getInstance().isModLoaded("easyshulkerboxes");
    public static final boolean IS_METAL_BUNDLES_MOD_LOADED = FabricLoader.getInstance().isModLoaded("metalbundles");

    private static final Identifier BUTTON_TEXTURE = new Identifier(ModOptions.MOD_ID, "textures/buttons.png");

    public static Optional<PosUpdatableButtonWidget> currentStackToNearbyContainersButton = Optional.empty();

    @Override
    public void onInitializeClient() {
        KeySequence.init();
        LockedSlots.init();
        InventoryActions.init();
        EndWorldTickExecutor.init();
        ForEachContainerTask.init();

        ScreenEvents.AFTER_INIT.register(this::addButtonsAndKeys);

        ModOptions.get().keymap.stackToNearbyContainersKey.registerNotOnScreen(InventoryActions::stackToNearbyContainers, ActionResult.PASS);
        ModOptions.get().keymap.restockFromNearbyContainersKey.registerNotOnScreen(InventoryActions::restockFromNearbyContainers, ActionResult.PASS);

        ModOptions.get().keymap.openModOptionsScreenKey.registerNotOnScreen(
                () -> MinecraftClient.getInstance().setScreen(new ModOptionsScreen(new ModOptionsGui())), ActionResult.FAIL);
    }

    private void addButtonsAndKeys(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        ModOptions.Appearance appearanceOption = ModOptions.get().appearance;
        boolean showButtonTooltip = appearanceOption.showButtonTooltip.booleanValue();

        if (screen instanceof AbstractInventoryScreen<?> inventoryScreen) {
            if (ModOptions.get().appearance.showStackToNearbyContainersButton.booleanValue()) {
                var buttonWidget = new PosUpdatableButtonWidget.Builder(inventoryScreen)
                        .setUV(0, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltip(showButtonTooltip ? getTooltipWithHint("stack-to-nearby-chests.tooltip.stackToNearbyContainersButton") : null)
                        .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                                parent.getY() + appearanceOption.stackToNearbyContainersButtonPosY.intValue()))
                        .setPressAction(button -> {
                            ScreenHandler screenHandler = inventoryScreen.getScreenHandler();
                            ItemStack cursorStack = screenHandler.getCursorStack();
                            if (cursorStack.isEmpty()) {
                                InventoryActions.stackToNearbyContainers();
                            } else {
                                Item item = cursorStack.getItem();

                                screenHandler.slots.stream()
                                        .filter(slot -> slot.inventory instanceof PlayerInventory)
                                        .filter(slot -> slot.getIndex() < 36) // 36: feet, 37: legs, 38: chest, 39: head, 40: offhand
                                        .filter(not(LockedSlots::isLocked))
                                        .filter(slot -> !slot.hasStack() || InventoryActions.canMerge(slot.getStack(), cursorStack))
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
                new PosUpdatableButtonWidget.Builder(inventoryScreen)
                        .setUV(16, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltip(showButtonTooltip ? getTooltipWithHint("stack-to-nearby-chests.tooltip.restockFromNearbyContainersButton") : null)
                        .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                                parent.getY() + appearanceOption.restockFromNearbyContainersButtonPosY.intValue()))
                        .setPressAction(button -> InventoryActions.restockFromNearbyContainers())
                        .build();
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                if (isTextFieldActive(scr) || isInventoryTabNotSelected(scr)) {
                    return;
                }

                ModOptions.Keymap keymap = ModOptions.get().keymap;

                boolean triggered = false;

                Slot focusedSlot = ((HandledScreenAccessor) inventoryScreen).getFocusedSlot();
                if (focusedSlot != null && focusedSlot.hasStack()) {
                    triggered = keymap.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey
                            .testThenRun(() -> InventoryActions.stackToNearbyContainers(focusedSlot.getStack().getItem()));
                }

                if (!triggered) {
                    keymap.stackToNearbyContainersKey.testThenRun(InventoryActions::stackToNearbyContainers);
                }

                keymap.restockFromNearbyContainersKey.testThenRun(InventoryActions::restockFromNearbyContainers);
            });
        } else if (isContainerScreen(screen)) {
            ScreenHandler screenHandler = ((HandledScreen<?>) screen).getScreenHandler();

            if (ModOptions.get().appearance.showQuickStackButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((HandledScreen<?>) screen)
                        .setUV(32, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltip(showButtonTooltip ? Text.translatable("stack-to-nearby-chests.tooltip.quickStackButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.quickStackButtonPosX, appearanceOption.quickStackButtonPosY))
                        .setPressAction(button -> InventoryActions.quickStack(screenHandler))
                        .build();
            }

            if (ModOptions.get().appearance.showRestockButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((HandledScreen<?>) screen)
                        .setUV(48, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltip(showButtonTooltip ? Text.translatable("stack-to-nearby-chests.tooltip.restockButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.restockButtonPosX, appearanceOption.restockButtonPosY))
                        .setPressAction(button -> InventoryActions.restock(screenHandler))
                        .build();
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                if (isTextFieldActive(scr) || isInventoryTabNotSelected(scr)) {
                    return;
                }

                ModOptions.get().keymap.quickStackKey.testThenRun(() -> InventoryActions.quickStack(screenHandler));
                ModOptions.get().keymap.restockKey.testThenRun(() -> InventoryActions.restock(screenHandler));
            });
        }
    }

    private static boolean isTextFieldActive(Screen screen) {
        Element focusedElement = screen.getFocused();

        if (focusedElement instanceof RecipeBookWidget) {
            TextFieldWidget searchField = ((RecipeBookWidgetAccessor) focusedElement).getSearchField();
            if (searchField != null && searchField.isActive()) {
                return true;
            }
        }

        return focusedElement instanceof TextFieldWidget textField && textField.isActive();
    }

    private static boolean isInventoryTabNotSelected(Screen screen) {
        return screen instanceof CreativeInventoryScreen creativeInventoryScreen
                && !creativeInventoryScreen.isInventoryTabSelected();
    }

    public static Vec2i getAbsolutePos(HandledScreenAccessor parent, ModOptions.IntOption x, ModOptions.IntOption y) {
        return new Vec2i(parent.getX() + parent.getBackgroundWidth() + x.intValue(),
                parent.getY() + parent.getBackgroundHeight() / 2 + y.intValue());
    }

    private static Text getTooltipWithHint(String translationKey) {
        return Text.translatable(translationKey)
                .append("\n")
                .append(Text.translatable("stack-to-nearby-chests.tooltip.hint").setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.DARK_GRAY)));
    }

    public static boolean isContainerScreen(Screen screen) {
        if (!(screen instanceof HandledScreen<?>)) {
            return false;
        } else if (
                screen instanceof BeaconScreen
                        || screen instanceof GrindstoneScreen
                        || screen instanceof CartographyTableScreen
                        || screen instanceof CraftingScreen
                        || screen instanceof LoomScreen
                        || screen instanceof EnchantmentScreen
                        || screen instanceof MerchantScreen
                        || screen instanceof ForgingScreen<?>
                        || screen instanceof StonecutterScreen
                        || screen instanceof AbstractInventoryScreen<?>
        ) {
            return false;
        } else if (screen instanceof HorseScreen) {
            return ((HorseScreenAccessor) screen).getEntity() instanceof AbstractDonkeyEntity abstractDonkeyEntity
                    && abstractDonkeyEntity.hasChest();
        }

        return true;
    }
}