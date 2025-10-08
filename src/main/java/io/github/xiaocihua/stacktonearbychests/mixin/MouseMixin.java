package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.KeySequence;
import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getWindow()Lnet/minecraft/client/util/Window;", ordinal = 0),
            cancellable = true)
    private void onOnMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        ActionResult result = switch (action) {
            case 0 -> OnKeyCallback.RELEASE.invoker().update(input.button() - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
            case 1 -> OnKeyCallback.PRESS.invoker().update(input.button() - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
            default -> ActionResult.PASS;
        };

        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
