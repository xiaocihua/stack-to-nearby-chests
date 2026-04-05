package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    private AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    public abstract T getMenu();

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void drawBefore(GuiGraphics context, Slot slot, CallbackInfo ci) {
        LockedSlots.drawFavoriteItemStyle(context, slot, true);
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void drawAfter(GuiGraphics context, Slot slot, CallbackInfo ci) {
        LockedSlots.drawFavoriteItemStyle(context, slot, false);
    }
}