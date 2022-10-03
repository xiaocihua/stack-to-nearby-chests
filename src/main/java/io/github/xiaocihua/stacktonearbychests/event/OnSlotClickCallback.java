package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface OnSlotClickCallback {

    Event<OnSlotClickCallback> BEFORE = EventFactory.createArrayBacked(OnSlotClickCallback.class,
            callbacks -> (slot, slotId, button, actionType, screenHandler) ->
                    EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(slot, slotId, button, actionType, screenHandler)));

    Event<OnSlotClickCallback> AFTER = EventFactory.createArrayBacked(OnSlotClickCallback.class,
            callbacks -> (slot, slotId, button, actionType, screenHandler) ->
                    EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(slot, slotId, button, actionType, screenHandler)));

    ActionResult update(Slot slot, int slotId, int button, SlotActionType actionType, ScreenHandler screenHandler);
}
