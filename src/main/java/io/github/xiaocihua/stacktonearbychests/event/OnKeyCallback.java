package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface OnKeyCallback {

    Event<OnKeyCallback> PRESS = EventFactory.createArrayBacked(OnKeyCallback.class,
            callbacks -> key -> EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(key)));

    Event<OnKeyCallback> RELEASE = EventFactory.createArrayBacked(OnKeyCallback.class,
            callbacks -> key -> EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(key)));

    ActionResult update(int key);
}
