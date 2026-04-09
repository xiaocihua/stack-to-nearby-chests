package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface OnKeyCallback {

    Event<OnKeyCallback> PRESS = EventFactory.createArrayBacked(OnKeyCallback.class,
            callbacks -> key -> EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(key)));

    Event<OnKeyCallback> RELEASE = EventFactory.createArrayBacked(OnKeyCallback.class,
            callbacks -> key -> EventUtil.forEachCallbackWithResult(callbacks, callback -> callback.update(key)));

    InteractionResult update(int key);
}
