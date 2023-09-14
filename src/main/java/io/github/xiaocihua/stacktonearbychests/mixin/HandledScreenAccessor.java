package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int getX();

    @Accessor("x")
    void setX(int x);

    @Accessor("y")
    int getY();

    @Accessor("y")
    void setY(int y);

    @Accessor("focusedSlot")
    @Nullable
    Slot getFocusedSlot();

    @Accessor
    int getBackgroundWidth();

    @Accessor
    int getBackgroundHeight();

    @Invoker
    @Nullable
    Slot invokeGetSlotAt(double x, double y);
}
