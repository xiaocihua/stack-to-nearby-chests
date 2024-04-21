package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface DisconnectCallback {

    Event<DisconnectCallback> EVENT = EventFactory.createArrayBacked(DisconnectCallback.class,
            callbacks -> () -> Callbacks.forEach(callbacks, DisconnectCallback::update));

    void update();
}
