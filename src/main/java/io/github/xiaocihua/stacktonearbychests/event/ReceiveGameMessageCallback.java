package io.github.xiaocihua.stacktonearbychests.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface ReceiveGameMessageCallback {

    Event<ReceiveGameMessageCallback> EVENT = EventFactory.createArrayBacked(ReceiveGameMessageCallback.class,
            listeners -> (message, overlay) ->
                    Callbacks.forEach(listeners, listener -> listener.onReceiveGameMessage(message, overlay)));

    void onReceiveGameMessage(Text message, boolean overlay);
}
