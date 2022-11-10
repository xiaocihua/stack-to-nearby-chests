package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.event.SetScreenCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket;<init>(Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V"), cancellable = true)
    private void onSwapItemWithOffhand(CallbackInfo ci) {
        if (LockedSlots.onSwapItemWithOffhand() == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        ActionResult result = SetScreenCallback.EVENT.invoker().update(screen);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
