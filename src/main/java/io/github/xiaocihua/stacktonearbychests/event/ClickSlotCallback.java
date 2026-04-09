package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

@FunctionalInterface
public interface ClickSlotCallback {

    Event<ClickSlotCallback> BEFORE = create();
    Event<ClickSlotCallback> AFTER = create();

    private static Event<ClickSlotCallback> create() {
        return EventFactory.createArrayBacked(ClickSlotCallback.class,
                callbacks -> (syncId, slotId, button, actionType, player) ->
                        EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(syncId, slotId, button, actionType, player)));
    }

    InteractionResult update(int syncId, int slotId, int button, ClickType actionType, Player player);
}
