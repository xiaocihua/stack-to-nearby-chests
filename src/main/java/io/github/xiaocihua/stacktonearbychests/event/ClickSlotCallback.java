package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;

@FunctionalInterface
public interface ClickSlotCallback {

    Event<ClickSlotCallback> BEFORE = create();
    Event<ClickSlotCallback> AFTER = create();

    private static Event<ClickSlotCallback> create() {
        return EventFactory.createArrayBacked(ClickSlotCallback.class,
                callbacks -> (syncId, slotId, button, actionType, player) ->
                        EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(syncId, slotId, button, actionType, player)));
    }

    ActionResult update(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player);
}
