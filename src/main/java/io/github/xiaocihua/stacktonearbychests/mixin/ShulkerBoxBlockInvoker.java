package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlock.class)
public interface ShulkerBoxBlockInvoker {

    @Invoker("canOpen")
    static boolean invokeCanOpen(BlockState state, World world, BlockPos pos, ShulkerBoxBlockEntity entity) {
        throw new AssertionError();
    }
}
