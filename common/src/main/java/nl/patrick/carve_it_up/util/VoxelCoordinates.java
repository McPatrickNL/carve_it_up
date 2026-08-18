// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/util/VoxelCoordinates.java
package nl.patrick.carve_it_up.util;

/**
 * Represents a target sub-voxel location within a 16x16x16 block grid.
 *
 * @param voxelX Integer index along the X axis ranging from 0 to 15.
 * @param voxelY Integer index along the Y axis ranging from 0 to 15.
 * @param voxelZ Integer index along the Z axis ranging from 0 to 15.
 */
// NewStart Data record for targeted sub-voxel coordinates
public record VoxelCoordinates(int voxelX, int voxelY, int voxelZ) {
    
    /**
     * Checks if the current coordinates sit within valid sub-voxel boundaries [0..15].
     *
     * @return True if all axes are within 0 and 15 inclusive.
     */
    public boolean isValidCoordinate() {
        // Validate that coordinates remain within the standard 16x16x16 grid bounds
        return voxelX >= 0 && voxelX < 16 && voxelY >= 0 && voxelY < 16 && voxelZ >= 0 && voxelZ < 16;
    }
}
// NewEnd