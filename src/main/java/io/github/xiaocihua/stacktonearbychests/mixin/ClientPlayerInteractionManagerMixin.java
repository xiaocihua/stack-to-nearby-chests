package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "clickSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/screen/ScreenHandler;slots:Lnet/minecraft/util/collection/DefaultedList;"), cancellable = true)
    private void beforeClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (LockedSlots.beforeClickSlot(slotId, button, actionType, player) == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void afterClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        LockedSlots.afterClickSlot(slotId, button, actionType, player);
    }

    @Inject(method = "setGameMode", at = @At("TAIL"))
    private void onSetGameMode(GameMode gameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }

    @Inject(method = "setGameModes", at = @At("TAIL"))
    private void onSetGameModes(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }
}
