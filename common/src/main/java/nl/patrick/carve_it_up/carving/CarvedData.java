// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvedData.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Structural container holding 3D voxel grid data, block materials, ownership,
 * and pre-computed collision/visual bounding shapes for a carved block.
 */
public class CarvedData {

    private final BlockState originalBlockState;
    private final Block mainBlock;
    private final List<Block> blocks = new ArrayList<>();

    // Dynamic Grid Support
    private final int resolution; // e.g. 16, 32, 64
    private final int totalVoxels; // resolution^3

    // Maps 3D voxel index (0 to totalVoxels - 1) to its specific material/block state.
    // Index formula: x + (y * resolution) + (z * resolution * resolution) where x,y,z are 0 to (resolution - 1).
    private final Map<Integer, BlockState> voxelMaterials = new HashMap<>();

    // Safety & Client Options
    private final UUID ownerUuid;
    private boolean bypassFactions = false;

    private VoxelShape visualShape;
    private VoxelShape collisionShape;
    private VoxelShape interactionShape;
    private int version = 1;

    // NewStart Primary constructor without level/shape lookups to eliminate recursive chunk loading deadlocks
    public CarvedData(BlockState originalBlockState, UUID ownerUuid, int resolution) {
        this.originalBlockState = originalBlockState;
        this.mainBlock = originalBlockState.getBlock();
        this.blocks.add(mainBlock);
        this.ownerUuid = ownerUuid;
        this.resolution = resolution;
        this.totalVoxels = resolution * resolution * resolution;

        this.visualShape = Shapes.block();
        this.collisionShape = Shapes.block();
        this.interactionShape = Shapes.block();

        // Populate initial grid with the original block state
        for (int i = 0; i < totalVoxels; i++) {
            this.voxelMaterials.put(i, originalBlockState);
        }
    }

    public CarvedData(BlockState originalBlockState, Level level, BlockPos blockPos, CollisionContext originalCollisionContext, UUID ownerUuid, int resolution) {
        this(originalBlockState, ownerUuid, resolution);
    }
    // NewEnd

    public BlockState getOriginalBlockState() {
        return originalBlockState;
    }

    public Block getMainBlock() {
        return mainBlock;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Map<Integer, BlockState> getVoxelMaterials() {
        return voxelMaterials;
    }

    public VoxelShape getVisualShape() {
        return visualShape;
    }

    public VoxelShape getCollisionShape() {
        return collisionShape;
    }

    public VoxelShape getInteractionShape() {
        return interactionShape;
    }

    public int getVersion() {
        return this.version;
    }

    public int getResolution() {
        return this.resolution;
    }

    public int getTotalVoxels() {
        return this.totalVoxels;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public boolean canBypassFactions() {
        return this.bypassFactions;
    }

    public void setVisualShape(VoxelShape visualShape) {
        this.visualShape = visualShape;
    }

    public void setCollisionShape(VoxelShape collisionShape) {
        this.collisionShape = collisionShape;
    }

    public void setInteractionShape(VoxelShape interactionShape) {
        this.interactionShape = interactionShape;
    }

    public void setBypassFactions(boolean bypass) {
        this.bypassFactions = bypass;
    }

    public void incrementVersion() {
        this.version++;
    }

    public boolean hasBlock(Block block) {
        return blocks.contains(block);
    }

    public boolean addBlock(Block block) {
        if (blocks.contains(block)) {
            return false;
        }
        blocks.add(block);
        return true;
    }

    public boolean removeBlock(Block block) {
        if (!blocks.contains(block) || block == this.mainBlock) {
            return false;
        }
        blocks.remove(block);
        return true;
    }
}