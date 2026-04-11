package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.KeySequence;
import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", ordinal = 0),
            cancellable = true)
    private void onOnMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        InteractionResult result = switch (action) {
            case 0 -> OnKeyCallback.RELEASE.invoker().update(input.button() - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
            case 1 -> OnKeyCallback.PRESS.invoker().update(input.button() - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
            default -> InteractionResult.PASS;
        };

        if (result == InteractionResult.FAIL) {
            ci.cancel();
        }
    }
}
