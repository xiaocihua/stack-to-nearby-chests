package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.event.ReceiveGameMessageCallback;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onOnGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        ReceiveGameMessageCallback.EVENT.invoker().onReceiveGameMessage(message, overlay);
    }
}
