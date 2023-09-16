package io.github.xiaocihua.stacktonearbychests;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBlocksInBox;
import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBox;

public class ForEachBlockContainerTask extends ForEachContainerTask {

    private final World world;
    private final ClientPlayerInteractionManager interactionManager;
    private final float squaredReachDistance;
    private final Entity cameraEntity;

    private final Collection<String> filter;

    private final Iterator<BlockPos> blocks;
    private final Set<BlockPos> searchedBlocks = new HashSet<>();
    private boolean hasSearchedEnderChest = false;

    public ForEachBlockContainerTask(MinecraftClient client,
                                     Entity cameraEntity,
                                     World world,
                                     ClientPlayerEntity player,
                                     ClientPlayerInteractionManager interactionManager,
                                     Consumer<ScreenHandler> action,
                                     Collection<String> filter
                                ) {
        super(client, player, action);
        this.world = world;
        this.interactionManager = interactionManager;
        float reachDistance = interactionManager.getReachDistance();
        this.squaredReachDistance = MathHelper.square(reachDistance);
        this.cameraEntity = cameraEntity;
        this.blocks = getBlocksInBox(getBox(cameraEntity.getCameraPosVec(0), reachDistance))
                .iterator();
        this.filter = filter;
    }

    @Override
    protected boolean openNextContainerInternal() {
        while (blocks.hasNext()) {
            BlockPos pos = blocks.next().toImmutable();

            if (searchedBlocks.contains(pos)) {
                continue;
            }
            if (!isOpenable(world, pos)) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            if (!filter.contains(Registries.BLOCK.getId(state.getBlock()).toString())) {
                continue;
            }

            Vec3d origin = cameraEntity.getCameraPosVec(0);
            Vec3d closestPos = MathUtil.getClosestPoint(pos, state.getOutlineShape(world, pos), origin);
            if (closestPos.squaredDistanceTo(origin) > squaredReachDistance) {
                continue;
            }

            searchedBlocks.add(pos);
            if (state.getBlock() == Blocks.ENDER_CHEST) {
                if (hasSearchedEnderChest) {
                    continue;
                }
                hasSearchedEnderChest = true;
            } else if (state.getBlock() == Blocks.SHULKER_BOX) {
                Direction facing = state.get(ShulkerBoxBlock.FACING);
                BlockPos facingBlockPos = pos.offset(facing);
                for (var dir : Direction.values()) {
                    if (dir == facing || dir == facing.getOpposite()) {
                        continue;
                    }

                    BlockPos adjacentBlockPos = facingBlockPos.offset(dir);
                    BlockState adjacentBlockState = world.getBlockState(adjacentBlockPos);
                    if (adjacentBlockState.getBlock() == Blocks.SHULKER_BOX
                            && adjacentBlockState.get(ShulkerBoxBlock.FACING) == dir.getOpposite()) {
                        searchedBlocks.add(adjacentBlockPos);
                    }
                }
            } else {
                getTheOtherHalfOfLargeChest(world, pos).ifPresent(searchedBlocks::add);
            }

            var hitResult = new BlockHitResult(closestPos, MathUtil.getFacingDirection(closestPos.subtract(origin)), pos, false);
            interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);

            return true;
        }

        return false;
    }

    private boolean isOpenable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof Inventory) && state.getBlock() != Blocks.ENDER_CHEST) {
            return false;
        }
        if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity && isShulkerBoxBlocked(state, world, pos, shulkerBoxBlockEntity)) {
            return false;
        }
        if (state.getBlock() instanceof ChestBlock || state.getBlock() == Blocks.ENDER_CHEST) {
            if (ChestBlock.isChestBlocked(world, pos)){
                return false;
            }
            return getTheOtherHalfOfLargeChest(world, pos)
                    .map(offsetPos -> !ChestBlock.isChestBlocked(world, offsetPos))
                    .orElse(true);
        }
        return true;
    }

    private boolean isShulkerBoxBlocked(BlockState state, World world, BlockPos pos, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        if (shulkerBoxBlockEntity.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
            return false;
        }

        Box box = ShulkerEntity.calculateBoundingBox(state.get(ShulkerBoxBlock.FACING), 0.0F, 0.5F).offset(pos).contract(1.0E-6);
        return !world.isSpaceEmpty(box);
    }

    private Optional<BlockPos> getTheOtherHalfOfLargeChest(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            // getFacing(BlockState) returns the direction in which the other half of the chest is located
            BlockPos offsetPos = pos.offset(ChestBlock.getFacing(state)); 
            BlockState theOtherHalf = world.getBlockState(offsetPos);
            if(theOtherHalf.getBlock() == state.getBlock()
                    && state.get(ChestBlock.FACING) == theOtherHalf.get(ChestBlock.FACING)
                    && ChestBlock.getFacing(state) == ChestBlock.getFacing(theOtherHalf).getOpposite()) {
                return Optional.ofNullable(offsetPos);
            }
        }
        return Optional.empty();
    }

}
