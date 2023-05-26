package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseScreen.class)
public interface HorseScreenAccessor {

    @Accessor
    AbstractHorseEntity getEntity();
}
