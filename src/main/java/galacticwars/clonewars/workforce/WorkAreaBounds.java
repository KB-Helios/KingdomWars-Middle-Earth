package galacticwars.clonewars.workforce;

import net.minecraft.core.BlockPos;

public record WorkAreaBounds(int width, int height, int depth) {
    public WorkAreaBounds {
        if (width < 1 || width > 64 || height < 1 || height > 64 || depth < 1 || depth > 64) {
            throw new IllegalArgumentException("work area dimensions must be between 1 and 64");
        }
    }

    public static WorkAreaBounds radius(int radius) {
        int diameter = Math.addExact(Math.multiplyExact(radius, 2), 1);
        int bounded = Math.min(64, diameter);
        return new WorkAreaBounds(bounded, bounded, bounded);
    }

    public boolean contains(BlockPos center, BlockPos target) {
        int minX = center.getX() - (width - 1) / 2;
        int maxX = center.getX() + width / 2;
        int minY = center.getY() - (height - 1) / 2;
        int maxY = center.getY() + height / 2;
        int minZ = center.getZ() - (depth - 1) / 2;
        int maxZ = center.getZ() + depth / 2;
        return target.getX() >= minX && target.getX() <= maxX
                && target.getY() >= minY && target.getY() <= maxY
                && target.getZ() >= minZ && target.getZ() <= maxZ;
    }

    public boolean contains(int centerX, int centerY, int centerZ, int targetX, int targetY, int targetZ) {
        int minX = centerX - (width - 1) / 2;
        int maxX = centerX + width / 2;
        int minY = centerY - (height - 1) / 2;
        int maxY = centerY + height / 2;
        int minZ = centerZ - (depth - 1) / 2;
        int maxZ = centerZ + depth / 2;
        return targetX >= minX && targetX <= maxX
                && targetY >= minY && targetY <= maxY
                && targetZ >= minZ && targetZ <= maxZ;
    }
}
