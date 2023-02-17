package io.github.xiaocihua.stacktonearbychests;

import com.mojang.logging.LogUtils;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;
import io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.List;

@Environment(EnvType.CLIENT)
public class StackToNearbyChests implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier BUTTON_TEXTURE = new Identifier(ModOptions.MOD_ID, "textures/buttons.png");

    @Override
    public void onInitializeClient() {
        KeySequence.init();
        LockedSlots.init();
        InventoryOps.init();

        ScreenEvents.AFTER_INIT.register(this::addButtonsAndKeys);

        ModOptions.get().keymap.stackToNearbyContainersKey.registerNotOnScreen(InventoryOps::stackToNearbyContainers, ActionResult.PASS);
        ModOptions.get().keymap.restockFromNearbyContainersKey.registerNotOnScreen(InventoryOps::restockFromNearbyContainers, ActionResult.PASS);

        ModOptions.get().keymap.openModOptionsScreenKey.registerNotOnScreen(
                () -> MinecraftClient.getInstance().setScreen(new ModOptionsScreen(new ModOptionsGui())), ActionResult.FAIL);
    }

    private void addButtonsAndKeys(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof HandledScreen<?>)) {
            return;
        }

        // For compatibility with PlayerEx
        String screenName = screen.getClass().getCanonicalName();
        if ("com.github.clevernucleus.playerex.client.gui.ExScreen".equals(screenName)
                || "com.github.clevernucleus.playerex.client.gui.CombatPageLayer".equals(screenName)
                || "com.github.clevernucleus.playerex.client.gui.AttributesPageLayer".equals(screenName)
        ) {
            return;
        }

        ModOptions.Appearance appearanceOption = ModOptions.get().appearance;

        if (screen instanceof AbstractInventoryScreen<?> inventoryScreen) {
            if (ModOptions.get().appearance.showStackToNearbyContainersButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder(inventoryScreen)
                        .setUV(0, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltipSupplier((button, matrices, mouseX, mouseY) ->
                                screen.renderTooltip(matrices, getLinesWithHint("stack-to-nearby-chests.stackToNearbyContainersButton.tooltip"), mouseX, mouseY))
                        .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                                parent.getY() + appearanceOption.stackToNearbyContainersButtonPosY.intValue()))
                        .setPressAction(button -> InventoryOps.stackToNearbyContainers())
                        .build();
            }

            if (ModOptions.get().appearance.showRestockFromNearbyContainersButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder(inventoryScreen)
                        .setUV(16, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltipSupplier((button, matrices, mouseX, mouseY) ->
                                screen.renderTooltip(matrices, getLinesWithHint("stack-to-nearby-chests.restockFromNearbyContainersButton.tooltip"), mouseX, mouseY))
                        .setPosUpdater(parent -> new Vec2i(parent.getX() + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                                parent.getY() + appearanceOption.restockFromNearbyContainersButtonPosY.intValue()))
                        .setPressAction(button -> InventoryOps.restockFromNearbyContainers())
                        .build();
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                ModOptions.get().keymap.stackToNearbyContainersKey.testThenRun(InventoryOps::stackToNearbyContainers);
                ModOptions.get().keymap.restockFromNearbyContainersKey.testThenRun(InventoryOps::restockFromNearbyContainers);
            });
        } else if (!(screen instanceof BeaconScreen
                || screen instanceof GrindstoneScreen
                || screen instanceof CartographyTableScreen
                || screen instanceof CraftingScreen
                || screen instanceof LoomScreen
                || screen instanceof EnchantmentScreen
                || screen instanceof MerchantScreen
                || screen instanceof ForgingScreen<?>
                || screen instanceof StonecutterScreen
                )) {
            ScreenHandler screenHandler = ((HandledScreen<?>) screen).getScreenHandler();

            if (ModOptions.get().appearance.showQuickStackButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((HandledScreen<?>) screen)
                        .setUV(32, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltipSupplier((button, matrices, mouseX, mouseY) ->
                                screen.renderTooltip(matrices, getLines("stack-to-nearby-chests.quickStackButton.tooltip"), mouseX, mouseY))
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.quickStackButtonPosX, appearanceOption.quickStackButtonPosY))
                        .setPressAction(button -> InventoryOps.quickStack(screenHandler))
                        .build();
            }

            if (ModOptions.get().appearance.showRestockButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((HandledScreen<?>) screen)
                        .setUV(48, 0)
                        .setTexture(BUTTON_TEXTURE, 64, 32)
                        .setTooltipSupplier((button, matrices, mouseX, mouseY) ->
                                screen.renderTooltip(matrices, getLines("stack-to-nearby-chests.restockButton.tooltip"), mouseX, mouseY))
                        .setPosUpdater(parent -> getAbsolutePos(parent, appearanceOption.restockButtonPosX, appearanceOption.restockButtonPosY))
                        .setPressAction(button -> InventoryOps.restock(screenHandler))
                        .build();
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                ModOptions.get().keymap.quickStackKey.testThenRun(() -> InventoryOps.quickStack(screenHandler));
                ModOptions.get().keymap.restockKey.testThenRun(() -> InventoryOps.restock(screenHandler));
            });
        }
    }

    private static Vec2i getAbsolutePos(HandledScreenAccessor parent, ModOptions.IntOption x, ModOptions.IntOption y) {
        return new Vec2i(parent.getX() + parent.getBackgroundWidth() + x.intValue(),
                parent.getY() + parent.getBackgroundHeight() / 2 + y.intValue());
    }

    private List<Text> getLines(String text) {
        return List.of(new TranslatableText(text));
    }

    private List<Text> getLinesWithHint(String text) {
        return List.of(new TranslatableText(text),
                new TranslatableText("stack-to-nearby-chests.tooltip.hint").setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.DARK_GRAY)));
    }
}