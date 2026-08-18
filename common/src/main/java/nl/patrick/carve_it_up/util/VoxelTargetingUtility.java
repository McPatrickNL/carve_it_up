// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/util/VoxelTargetingUtility.java
package nl.patrick.carve_it_up.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Utility containing methods for calculating targeted blocks and extracting exact sub-voxel positions.
 */
public class VoxelTargetingUtility {
    
    // Future Allow custom reach distances configured via player attributes or tool item properties
    
    /**
     * Multiplier to scale normalized block coordinates [0.0, 1.0] to a standard 16x16x16 sub-voxel grid.
     */
    public static final double VOXEL_GRID_SCALE = 16.0;
    
    /**
     * Small offset applied inward to prevent rounding errors on exact face boundary hit points.
     */
    private static final double BOUNDARY_EPSILON = 0.0001;
    
    /**
     * Performs a raycast from the player's line of sight to identify targeted block geometry.
     *
     * @param levelWorld The active world level instance.
     * @param playerEntity Player executing the targeting check.
     * @param reachDistanceInBlocks Maximum reach distance in blocks.
     * @return BlockHitResult detailing hit point, hit face, and targeted BlockPos.
     */
    public static BlockHitResult performBlockRaycast(Level levelWorld, Player playerEntity, double reachDistanceInBlocks) {
        // Retrieve origin vector at player eye height
        Vec3 eyePositionVector = playerEntity.getEyePosition(1.0F);
        
        // Retrieve current look vector of the player
        Vec3 viewDirectionVector = playerEntity.getViewVector(1.0F);
        
        // Calculate ray end point by extending along look vector by reach distance
        // NewStart Fixed undefined "reachDirectionVector" symbol - was a typo causing a compile error, now correctly uses reachDistanceInBlocks on all three axes
        Vec3 endRaycastVector = eyePositionVector.add(
            viewDirectionVector.x * reachDistanceInBlocks,
            viewDirectionVector.y * reachDistanceInBlocks,
            viewDirectionVector.z * reachDistanceInBlocks
                                                     );
        // NewEnd
        
        // Build clip context using block outline shapes and ignoring fluid interaction
        ClipContext raycastContext = new ClipContext(
            eyePositionVector,
            endRaycastVector,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            playerEntity
        );
        
        // Future Cross-check context requirements if liquid-carving features are added.
        return levelWorld.clip(raycastContext);
    }
    
    /**
     * Converts a BlockHitResult into exact 16x16x16 sub-voxel grid coordinates.
     *
     * @param blockHitResult The raycast result from performing a block hit test.
     * @return VoxelCoordinates instance with sub-voxel indices, or null if no block was struck.
     */
    public static VoxelCoordinates extractTargetedVoxel(BlockHitResult blockHitResult) {
        // Check if hit result exists and struck a valid block
        if (blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS) {
            return null;
        }
        
        // Retrieve block position in world coordinate space
        BlockPos targetBlockPos = blockHitResult.getBlockPos();
        
        // Retrieve exact collision point vector in world space
        Vec3 hitLocationVector = blockHitResult.getLocation();
        
        // Retrieve the impacted block face direction
        Direction hitDirection = blockHitResult.getDirection();
        
        // Determine local coordinate relative to block origin [0.0, 1.0]
        double localBlockXCoordinate = hitLocationVector.x - targetBlockPos.getX();
        double localBlockYCoordinate = hitLocationVector.y - targetBlockPos.getY();
        double localBlockZCoordinate = hitLocationVector.z - targetBlockPos.getZ();
        
        // Offset slightly inward along face normal to ensure exact hit lands inside target voxel
        localBlockXCoordinate -= hitDirection.getStepX() * BOUNDARY_EPSILON;
        localBlockYCoordinate -= hitDirection.getStepY() * BOUNDARY_EPSILON;
        localBlockZCoordinate -= hitDirection.getStepZ() * BOUNDARY_EPSILON;
        
        // Scale coordinates to 0-16 grid and clamp values strictly to [0, 15]
        int calculatedVoxelX = Math.min(15, Math.max(0, (int) Math.floor(localBlockXCoordinate * VOXEL_GRID_SCALE)));
        int calculatedVoxelY = Math.min(15, Math.max(0, (int) Math.floor(localBlockYCoordinate * VOXEL_GRID_SCALE)));
        int calculatedVoxelZ = Math.min(15, Math.max(0, (int) Math.floor(localBlockZCoordinate * VOXEL_GRID_SCALE)));
        
        return new VoxelCoordinates(calculatedVoxelX, calculatedVoxelY, calculatedVoxelZ);
    }
}