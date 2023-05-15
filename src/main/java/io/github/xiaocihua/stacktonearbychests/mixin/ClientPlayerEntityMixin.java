package io.github.xiaocihua.stacktonearbychests.mixin;

import com.mojang.authlib.GameProfile;
import io.github.xiaocihua.stacktonearbychests.InventoryOps;
import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void beforeDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (LockedSlots.beforeDropSelectedItem(getInventory().selectedSlot) == ActionResult.FAIL) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "dropSelectedItem", at = @At("TAIL"))
    private void afterDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        LockedSlots.afterDropSelectedItem(getInventory().selectedSlot);
    }

    @Inject(method = "isSneaking", at = @At("RETURN"), cancellable = true)
    private void onIsSneaking(CallbackInfoReturnable<Boolean> cir) {
        if (!InventoryOps.isTerminated() && InventoryOps.sneak) {
            cir.setReturnValue(true);
        }
    }
}
