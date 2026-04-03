package io.github.xiaocihua.stacktonearbychests.event;

import net.minecraft.world.InteractionResult;

import java.util.function.Consumer;
import java.util.function.Function;

public class EventUtil {

    public static <T> InteractionResult forEachCallbackWithResult(T[] callbacks, Function<T, InteractionResult> function) {
        for (T callback : callbacks) {
            InteractionResult result = function.apply(callback);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    public static <T> void forEachCallback(T[] callbacks, Consumer<T> consumer) {
        for (T callback : callbacks) {
            consumer.accept(callback);
        }
    }
}
