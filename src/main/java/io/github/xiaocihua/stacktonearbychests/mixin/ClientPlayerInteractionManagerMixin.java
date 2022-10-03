package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "setGameMode", at = @At("TAIL"))
    private void onSetGameMode(GameMode gameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }

    @Inject(method = "setGameModes", at = @At("TAIL"))
    private void onSetGameModes(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }
}
