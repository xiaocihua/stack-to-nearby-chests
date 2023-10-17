package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.ForEachContainerTask;
import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ScreenHandler.class)
@Environment(EnvType.CLIENT)
public abstract class ScreenHandlerMixin {

    @Inject(method = "updateSlotStacks(ILjava/util/List;Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void onUpdateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
        if (ForEachContainerTask.isRunning()) {
            ForEachContainerTask.getCurrentTask().onInventory((ScreenHandler)(Object)this);
        }
    }

    @Inject(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;markDirty()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir, boolean bl, int i, Slot slot) {
        LockedSlots.onInsertItem(slot);
    }
}
