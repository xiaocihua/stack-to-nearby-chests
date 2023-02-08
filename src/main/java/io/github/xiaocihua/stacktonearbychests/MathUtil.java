package io.github.xiaocihua.stacktonearbychests;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

public class MathUtil {

    public static Box getBox(Vec3d center, double radius) {
        return new Box(
            center.x - radius,
            center.y - radius,
            center.z - radius,
            center.x + radius,
            center.y + radius,
            center.z + radius
        );
    }

    public static Iterable<BlockPos> getBlocksInBox(Box box) {
        int minX = floor(box.minX);
        int maxX = floor(box.maxX);
        int minY = floor(box.minY);
        int maxY = floor(box.maxY);
        int minZ = floor(box.minZ);
        int maxZ = floor(box.maxZ);
        return BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Direction getFacingDirection(Vec3d vec) {
        return Direction.getFacing(vec.x, vec.y, vec.z);
    }

    /**
     * Get the closest point to the {@code pos} on the block. From Earthcomputer's
     * <a href = "https://github.com/Earthcomputer/clientcommands">ClientCommands</a>.
     */
    public static Vec3d getClosestPoint(BlockPos blockPos, VoxelShape voxel, Vec3d pos) {
        return getClosestPoint(blockPos, voxel, pos, null);
    }

    public static Vec3d getClosestPoint(BlockPos blockPos, VoxelShape voxel, Vec3d pos, Direction dir) {
        ClosestPosResult result = new ClosestPosResult();
        Direction[] dirs = dir == null ? Direction.values() : new Direction[]{dir};
        voxel.forEachBox((x1, y1, z1, x2, y2, z2) -> {
            Box box = new Box(x1, y1, z1, x2, y2, z2).offset(blockPos);
            for (Direction face : dirs) {
                Box faceBox = getFace(box, face);
                // Since the faces are axis aligned, it's a simple clamp operation
                Vec3d val = new Vec3d(clamp(pos.x, faceBox.minX, faceBox.maxX),
                        clamp(pos.y, faceBox.minY, faceBox.maxY),
                        clamp(pos.z, faceBox.minZ, faceBox.maxZ));
                double distanceSq = val.squaredDistanceTo(pos);
                if (distanceSq < result.distanceSq) {
                    result.val = val;
                    result.distanceSq = distanceSq;
                }
            }
        });
        return result.val;
    }

    /**
     * From Earthcomputer's <a href = "https://github.com/Earthcomputer/clientcommands">ClientCommands</a>.
     */
    private static Box getFace(Box box, Direction dir) {
        return switch (dir) {
            case WEST -> new Box(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
            case EAST -> new Box(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
            case DOWN -> new Box(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
            case UP -> new Box(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            case NORTH -> new Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
            case SOUTH -> new Box(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        };
    }

    private static class ClosestPosResult {
        Vec3d val;
        double distanceSq = Double.POSITIVE_INFINITY;
    }
}
