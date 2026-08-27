// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvedBlockRotator.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles 3D voxel rotations and geometric state transitions for interactive blocks
 * such as doors, trapdoors, fence gates, buttons, and pressure plates.
 */
public class CarvedBlockRotator {

    /**
     * Checks whether a block state transition is an interactive state change.
     */
    public static boolean isInteractiveStateTransition(BlockState oldState, BlockState newState) {
        if (oldState == null || newState == null || oldState.getBlock() != newState.getBlock()) {
            return false;
        }

        if (oldState.getBlock() instanceof DoorBlock && oldState.hasProperty(DoorBlock.OPEN) && newState.hasProperty(DoorBlock.OPEN)) {
            return oldState.getValue(DoorBlock.OPEN) != newState.getValue(DoorBlock.OPEN);
        }

        if (oldState.getBlock() instanceof TrapDoorBlock && oldState.hasProperty(TrapDoorBlock.OPEN) && newState.hasProperty(TrapDoorBlock.OPEN)) {
            return oldState.getValue(TrapDoorBlock.OPEN) != newState.getValue(TrapDoorBlock.OPEN);
        }

        if (oldState.getBlock() instanceof FenceGateBlock && oldState.hasProperty(FenceGateBlock.OPEN) && newState.hasProperty(FenceGateBlock.OPEN)) {
            return oldState.getValue(FenceGateBlock.OPEN) != newState.getValue(FenceGateBlock.OPEN);
        }

        if ((oldState.getBlock() instanceof ButtonBlock || oldState.getBlock() instanceof PressurePlateBlock)
            && oldState.hasProperty(ButtonBlock.POWERED) && newState.hasProperty(ButtonBlock.POWERED)) {
            return oldState.getValue(ButtonBlock.POWERED) != newState.getValue(ButtonBlock.POWERED);
        }

        return false;
    }

    /**
     * Applies the appropriate 3D voxel rotation or translation transform to CarvedData during an interactive state change.
     */
    public static boolean applyStateTransition(CarvedData carvedData, BlockState oldState, BlockState newState) {
        if (!isInteractiveStateTransition(oldState, newState)) {
            return false;
        }

        int resolution = carvedData.getResolution();
        int maxCoord = resolution - 1;
        Map<Integer, BlockState> oldVoxels = new HashMap<>(carvedData.getVoxelMaterials());
        Map<Integer, BlockState> newVoxels = new HashMap<>();

        boolean handled = false;

        // --- 1. DOOR ROTATION ---
        if (oldState.getBlock() instanceof DoorBlock && oldState.hasProperty(DoorBlock.OPEN) && newState.hasProperty(DoorBlock.OPEN)) {
            Direction facing = oldState.getValue(DoorBlock.FACING);
            DoorHingeSide hinge = oldState.hasProperty(DoorBlock.HINGE) ? oldState.getValue(DoorBlock.HINGE) : DoorHingeSide.LEFT;

            // Determine if the door pivot is at (0, 0) / (15, 15) or at (15, 0) / (0, 15)
            boolean isDiagonalSwap;
            switch (facing) {
                case NORTH -> isDiagonalSwap = (hinge == DoorHingeSide.LEFT);  // Pivot (0, 0) -> (z, x) vs Pivot (15, 0) -> (15-z, 15-x)
                case SOUTH -> isDiagonalSwap = (hinge == DoorHingeSide.LEFT);  // Pivot (15, 15) -> (z, x) vs Pivot (0, 15) -> (15-z, 15-x)
                case EAST  -> isDiagonalSwap = (hinge == DoorHingeSide.RIGHT); // Pivot (15, 15) -> (z, x) vs Pivot (15, 0) -> (15-z, 15-x)
                case WEST  -> isDiagonalSwap = (hinge == DoorHingeSide.RIGHT); // Pivot (0, 0) -> (z, x) vs Pivot (0, 15) -> (15-z, 15-x)
                default    -> isDiagonalSwap = true;
            }

            for (Map.Entry<Integer, BlockState> entry : oldVoxels.entrySet()) {
                int index = entry.getKey();
                int x = index % resolution;
                int y = (index / resolution) % resolution;
                int z = index / (resolution * resolution);

                int rx = isDiagonalSwap ? z : (maxCoord - z);
                int rz = isDiagonalSwap ? x : (maxCoord - x);

                if (rx >= 0 && rx < resolution && rz >= 0 && rz < resolution) {
                    int newIndex = rx + (y * resolution) + (rz * resolution * resolution);
                    newVoxels.put(newIndex, entry.getValue());
                }
            }

            handled = true;
        }

        // --- 2. TRAPDOOR ROTATION (Opens UP/DOWN against the hinge wall away from player) ---
        else if (oldState.getBlock() instanceof TrapDoorBlock && oldState.hasProperty(TrapDoorBlock.OPEN) && newState.hasProperty(TrapDoorBlock.OPEN)) {
            Direction facing = oldState.getValue(TrapDoorBlock.FACING);
            Half half = oldState.hasProperty(TrapDoorBlock.HALF) ? oldState.getValue(TrapDoorBlock.HALF) : Half.BOTTOM;

            for (Map.Entry<Integer, BlockState> entry : oldVoxels.entrySet()) {
                int index = entry.getKey();
                int x = index % resolution;
                int y = (index / resolution) % resolution;
                int z = index / (resolution * resolution);

                int rx = x;
                int ry = y;
                int rz = z;

                if (half == Half.BOTTOM) {
                    // Hinge at bottom Y = 0 against the wall opposite of facing
                    switch (facing) {
                        case NORTH -> {
                            // Wall at South (Z=15): swings UP against South wall
                            ry = maxCoord - z;
                            rz = maxCoord - y;
                        }
                        case SOUTH -> {
                            // Wall at North (Z=0): swings UP against North wall
                            ry = z;
                            rz = y;
                        }
                        case WEST -> {
                            // Wall at East (X=15): swings UP against East wall
                            ry = maxCoord - x;
                            rx = maxCoord - y;
                        }
                        case EAST -> {
                            // Wall at West (X=0): swings UP against West wall
                            ry = x;
                            rx = y;
                        }
                        default -> {}
                    }
                } else {
                    // Hinge at ceiling Y = 15 against the wall opposite of facing
                    switch (facing) {
                        case NORTH -> {
                            // Wall at South (Z=15): swings DOWN against South wall
                            ry = z;
                            rz = y;
                        }
                        case SOUTH -> {
                            // Wall at North (Z=0): swings DOWN against North wall
                            ry = maxCoord - z;
                            rz = maxCoord - y;
                        }
                        case WEST -> {
                            // Wall at East (X=15): swings DOWN against East wall
                            ry = x;
                            rx = y;
                        }
                        case EAST -> {
                            // Wall at West (X=0): swings DOWN against West wall
                            ry = maxCoord - x;
                            rx = maxCoord - y;
                        }
                        default -> {}
                    }
                }

                if (rx >= 0 && rx < resolution && ry >= 0 && ry < resolution && rz >= 0 && rz < resolution) {
                    int newIndex = rx + (ry * resolution) + (rz * resolution * resolution);
                    newVoxels.put(newIndex, entry.getValue());
                }
            }

            handled = true;
        }

        // --- 3. FENCE GATE ROTATION ---
        else if (oldState.getBlock() instanceof FenceGateBlock && oldState.hasProperty(FenceGateBlock.OPEN) && newState.hasProperty(FenceGateBlock.OPEN)) {
            for (Map.Entry<Integer, BlockState> entry : oldVoxels.entrySet()) {
                int index = entry.getKey();
                int x = index % resolution;
                int y = (index / resolution) % resolution;
                int z = index / (resolution * resolution);

                int rx = z;
                int rz = x;

                if (rx >= 0 && rx < resolution && rz >= 0 && rz < resolution) {
                    int newIndex = rx + (y * resolution) + (rz * resolution * resolution);
                    newVoxels.put(newIndex, entry.getValue());
                }
            }

            handled = true;
        }

        // --- 4. BUTTONS & PRESSURE PLATES (Press into wall / down into floor) ---
        else if ((oldState.getBlock() instanceof ButtonBlock || oldState.getBlock() instanceof PressurePlateBlock)
            && oldState.hasProperty(ButtonBlock.POWERED) && newState.hasProperty(ButtonBlock.POWERED)) {

            boolean wasPowered = oldState.getValue(ButtonBlock.POWERED);
            boolean isPowering = !wasPowered && newState.getValue(ButtonBlock.POWERED);
            int step = isPowering ? 1 : -1;

            Direction pushDirection = Direction.DOWN;
            if (oldState.getBlock() instanceof ButtonBlock) {
                AttachFace face = oldState.getValue(ButtonBlock.FACE);
                Direction facing = oldState.getValue(ButtonBlock.FACING);
                if (face == AttachFace.FLOOR) {
                    pushDirection = Direction.DOWN;
                } else if (face == AttachFace.CEILING) {
                    pushDirection = Direction.UP;
                } else {
                    // Pushes INTO the wall (opposite of facing direction)
                    pushDirection = facing.getOpposite();
                }
            }

            int dx = pushDirection.getStepX() * step;
            int dy = pushDirection.getStepY() * step;
            int dz = pushDirection.getStepZ() * step;

            for (Map.Entry<Integer, BlockState> entry : oldVoxels.entrySet()) {
                int index = entry.getKey();
                int x = index % resolution;
                int y = (index / resolution) % resolution;
                int z = index / (resolution * resolution);

                int rx = Math.min(maxCoord, Math.max(0, x + dx));
                int ry = Math.min(maxCoord, Math.max(0, y + dy));
                int rz = Math.min(maxCoord, Math.max(0, z + dz));

                int newIndex = rx + (ry * resolution) + (rz * resolution * resolution);
                newVoxels.put(newIndex, entry.getValue());
            }

            handled = true;
        }

        if (handled && !newVoxels.isEmpty()) {
            carvedData.getVoxelMaterials().clear();
            carvedData.getVoxelMaterials().putAll(newVoxels);
            carvedData.setOriginalBlockState(newState);
            carvedData.rebuildBlockPalette();
            return true;
        }

        return false;
    }
}
