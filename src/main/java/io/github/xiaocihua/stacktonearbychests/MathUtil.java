package io.github.xiaocihua.stacktonearbychests;

import static net.minecraft.util.Mth.clamp;
import static net.minecraft.util.Mth.floor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MathUtil {

    public static AABB getBox(Vec3 center, double radius) {
        return new AABB(
            center.x - radius,
            center.y - radius,
            center.z - radius,
            center.x + radius,
            center.y + radius,
            center.z + radius
        );
    }

    public static Iterable<BlockPos> getBlocksInBox(AABB box) {
        int minX = floor(box.minX);
        int maxX = floor(box.maxX);
        int minY = floor(box.minY);
        int maxY = floor(box.maxY);
        int minZ = floor(box.minZ);
        int maxZ = floor(box.maxZ);
        return BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Direction getFacingDirection(Vec3 vec) {
        return Direction.getApproximateNearest(vec.x, vec.y, vec.z);
    }

    /**
     * Get the closest point to the {@code pos} on the block. From Earthcomputer's
     * <a href = "https://github.com/Earthcomputer/clientcommands">ClientCommands</a>.
     */
    public static Vec3 getClosestPoint(BlockPos blockPos, VoxelShape voxel, Vec3 pos) {
        return getClosestPoint(blockPos, voxel, pos, null);
    }

    public static Vec3 getClosestPoint(BlockPos blockPos, VoxelShape voxel, Vec3 pos, Direction dir) {
        ClosestPosResult result = new ClosestPosResult();
        Direction[] dirs = dir == null ? Direction.values() : new Direction[]{dir};
        voxel.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            AABB box = new AABB(x1, y1, z1, x2, y2, z2).move(blockPos);
            for (Direction face : dirs) {
                AABB faceBox = getFace(box, face);
                // Since the faces are axis aligned, it's a simple clamp operation
                Vec3 val = new Vec3(clamp(pos.x, faceBox.minX, faceBox.maxX),
                        clamp(pos.y, faceBox.minY, faceBox.maxY),
                        clamp(pos.z, faceBox.minZ, faceBox.maxZ));
                double distanceSq = val.distanceToSqr(pos);
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
    private static AABB getFace(AABB box, Direction dir) {
        return switch (dir) {
            case WEST -> new AABB(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
            case EAST -> new AABB(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
            case DOWN -> new AABB(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
            case UP -> new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            case NORTH -> new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
            case SOUTH -> new AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        };
    }

    private static class ClosestPosResult {
        Vec3 val;
        double distanceSq = Double.POSITIVE_INFINITY;
    }
}
