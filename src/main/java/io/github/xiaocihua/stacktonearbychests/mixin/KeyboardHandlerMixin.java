package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), cancellable = true)
    private void onOnKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        InteractionResult result = switch (action) {
            case 0 -> OnKeyCallback.RELEASE.invoker().update(input.key());
            case 1 -> OnKeyCallback.PRESS.invoker().update(input.key());
            default -> InteractionResult.PASS;
        };

        if (result == InteractionResult.FAIL) {
            ci.cancel();
        }
    }
}
