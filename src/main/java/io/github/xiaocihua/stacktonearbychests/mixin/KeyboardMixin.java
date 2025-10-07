package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0), cancellable = true)
    private void onOnKey(long window, int action, KeyInput input, CallbackInfo ci) {
        ActionResult result = switch (action) {
            case 0 -> OnKeyCallback.RELEASE.invoker().update(input.key());
            case 1 -> OnKeyCallback.PRESS.invoker().update(input.key());
            default -> ActionResult.PASS;
        };

        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
