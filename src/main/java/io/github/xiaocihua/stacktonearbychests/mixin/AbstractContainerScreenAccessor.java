package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("leftPos")
    int getX();

    @Accessor("leftPos")
    void setX(int x);

    @Accessor("topPos")
    int getY();

    @Accessor("topPos")
    void setY(int y);

    @Accessor("hoveredSlot")
    @Nullable
    Slot getFocusedSlot();

    @Accessor
    int getImageWidth();

    @Accessor
    int getImageHeight();

    @Invoker
    @Nullable
    Slot invokeGetHoveredSlot(double x, double y);
}
