package io.github.xiaocihua.stacktonearbychests;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;

public class EmiPluginImpl implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            ModOptions.Appearance appearanceOption = ModOptions.get().appearance;

            if (screen instanceof AbstractInventoryScreen<?> inventoryScreen) {
                int parentX = ((HandledScreenAccessor) inventoryScreen).getX();
                int parentY = ((HandledScreenAccessor) inventoryScreen).getY();

                if (ModOptions.get().appearance.showStackToNearbyContainersButton.booleanValue()) {
                    consumer.accept(new Bounds(
                            parentX + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                            parentY + appearanceOption.stackToNearbyContainersButtonPosY.intValue(),
                            16, 16
                    ));
                }

                if (ModOptions.get().appearance.showRestockFromNearbyContainersButton.booleanValue()) {
                    consumer.accept(new Bounds(
                            parentX + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                            parentY + appearanceOption.restockFromNearbyContainersButtonPosY.intValue(),
                            16, 16
                    ));
                }
            } else if (StackToNearbyChests.isContainerScreen(screen)) {
                if (ModOptions.get().appearance.showQuickStackButton.booleanValue()) {
                    Vec2i buttonPos = StackToNearbyChests.getAbsolutePos((HandledScreenAccessor) screen, appearanceOption.quickStackButtonPosX, appearanceOption.quickStackButtonPosY);
                    consumer.accept(new Bounds(buttonPos.x(), buttonPos.y(), 16, 16));
                }

                if (ModOptions.get().appearance.showRestockButton.booleanValue()) {
                    Vec2i buttonPos = StackToNearbyChests.getAbsolutePos((HandledScreenAccessor) screen, appearanceOption.restockButtonPosX, appearanceOption.restockButtonPosY);
                    consumer.accept(new Bounds(buttonPos.x(), buttonPos.y(), 16, 16));
                }
            }
        });
    }
}
