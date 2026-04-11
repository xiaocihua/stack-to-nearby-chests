package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface SetScreenCallback {

    Event<SetScreenCallback> EVENT = EventFactory.createArrayBacked(SetScreenCallback.class,
            listeners -> screen -> EventUtil.forEachCallbackWithResult(listeners, listener -> listener.update(screen)));

    InteractionResult update(Screen screen);
}
