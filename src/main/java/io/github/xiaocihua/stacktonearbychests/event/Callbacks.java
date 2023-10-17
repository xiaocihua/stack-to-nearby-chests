package io.github.xiaocihua.stacktonearbychests.event;

import net.minecraft.util.ActionResult;

import java.util.function.Consumer;
import java.util.function.Function;

public class Callbacks {

    public static <T> ActionResult forEachWithResult(T[] callbacks, Function<T, ActionResult> function) {
        for (T callback : callbacks) {
            ActionResult result = function.apply(callback);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    }

    public static <T> void forEach(T[] callbacks, Consumer<T> consumer) {
        for (T callback : callbacks) {
            consumer.accept(callback);
        }
    }
}
