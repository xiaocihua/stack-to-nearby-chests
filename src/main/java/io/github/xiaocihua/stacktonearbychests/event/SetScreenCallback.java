package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface SetScreenCallback {

    Event<SetScreenCallback> EVENT = EventFactory.createArrayBacked(SetScreenCallback.class,
            listeners -> screen -> Callbacks.forEachWithResult(listeners, listener -> listener.update(screen)));

    ActionResult update(Screen screen);
}
