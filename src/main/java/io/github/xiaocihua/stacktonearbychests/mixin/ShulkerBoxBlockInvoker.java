package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlock.class)
public interface ShulkerBoxBlockInvoker {

    @Invoker("canOpen")
    static boolean invokeCanOpen(BlockState state, Level world, BlockPos pos, ShulkerBoxBlockEntity entity) {
        throw new AssertionError();
    }
}
