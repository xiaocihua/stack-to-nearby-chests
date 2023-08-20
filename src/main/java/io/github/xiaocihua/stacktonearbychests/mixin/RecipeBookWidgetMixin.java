package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.ForEachContainerTask;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        if (ForEachContainerTask.isRunning()) {
            // Prevent current screen handler from being converted to AbstractRecipeScreenHandler
            ci.cancel();
        }
    }
}