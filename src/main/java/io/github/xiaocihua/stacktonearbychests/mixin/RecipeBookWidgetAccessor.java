package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookWidgetAccessor {

    @Accessor
    @Nullable
    TextFieldWidget getSearchField();
}
