package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.event.ClickSlotCallback;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "handleContainerInput", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;slots:Lnet/minecraft/core/NonNullList;"), cancellable = true)
    private void beforeClickSlot(int syncId, int slotId, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (ClickSlotCallback.BEFORE.invoker().update(syncId, slotId, button, actionType, player) == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
    private void afterClickSlot(int syncId, int slotId, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (ClickSlotCallback.AFTER.invoker().update(syncId, slotId, button, actionType, player) == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V", at = @At("HEAD"), cancellable = true)
    private void beforeDropSelectedItem(LocalPlayer player, boolean entireStack, CallbackInfo ci) {
        if (LockedSlots.beforeDropSelectedItem(player.getInventory().getSelectedSlot()) == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V", at = @At("TAIL"))
    private void afterDropSelectedItem(LocalPlayer player, boolean entireStack, CallbackInfo ci) {
        LockedSlots.afterDropSelectedItem(player.getInventory().getSelectedSlot());
    }

    @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V", at = @At("TAIL"))
    private void onSetGameMode(GameType gameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }

    @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V", at = @At("TAIL"))
    private void onSetGameModes(GameType gameMode, GameType previousGameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }
}
