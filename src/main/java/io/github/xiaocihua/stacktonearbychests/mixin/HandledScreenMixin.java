package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    public abstract T getScreenHandler();

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/screen/slot/Slot;)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void beforeDrawSlot(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k, Slot slot) {
        LockedSlots.drawFavoriteItemStyle(matrices, slot, false);
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void afterDrawSlot(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k, Slot slot) {
        LockedSlots.drawFavoriteItemStyle(matrices, slot, true);
    }
}