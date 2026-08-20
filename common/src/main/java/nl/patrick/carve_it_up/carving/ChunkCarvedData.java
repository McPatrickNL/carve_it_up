// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ChunkCarvedData.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;

/**
 * Thread-safe container attached to LevelChunk holding all carved data maps for blocks in that chunk.
 */
public class ChunkCarvedData { // Converted from Allman style brace

    // NewStart Thread-safe concurrent map allowing asynchronous chunk rendering threads to read while main thread updates
    private final Map<BlockPos, CarvedDataMapSet> carvedBlocks = new ConcurrentHashMap<>();
    // NewEnd

    private volatile boolean hasCarvedBlocks = false;

    /**
     * Associates a carved data instance with a specific block position within this chunk.
     *
     * @param worldLevel The active world level
     * @param targetBlockPos The position of the carved block
     * @param carvedData The carved data structure containing voxels
     */
    public void addCarvedData(Level worldLevel, BlockPos targetBlockPos, CarvedData carvedData) {
        this.carvedBlocks.put(targetBlockPos.immutable(), new CarvedDataMapSet(worldLevel, carvedData));
        this.hasCarvedBlocks = true;
        LOGGER.info("Carved data added to {}", targetBlockPos.toShortString());
    }

    /**
     * Removes carved data for a specific block position.
     *
     * @param targetBlockPos The position to remove
     */
    public void removeCarvedData(BlockPos targetBlockPos) {
        this.carvedBlocks.remove(targetBlockPos.immutable());
        if (this.carvedBlocks.isEmpty()) {
            this.hasCarvedBlocks = false;
            LOGGER.info("Carved data removed from {}", targetBlockPos.toShortString());
        }
    }

    /**
     * Resets the entire container back to an empty state.
     */
    public void clear() {
        this.carvedBlocks.clear();
        this.hasCarvedBlocks = false;
        LOGGER.info("All carved data wiped");
    }

    /**
     * Retrieves the carved data for a given block position if present.
     *
     * @param targetBlockPos The position to query
     * @return The CarvedData or null if not carved
     */
    public CarvedData getCarvedData(BlockPos targetBlockPos) {
        if (!isPositionCarved(targetBlockPos)) {
            return null;
        }
        CarvedDataMapSet mapSet = this.carvedBlocks.get(targetBlockPos);
        return mapSet != null ? mapSet.getCarvedData() : null;
    }

    public Map<BlockPos, CarvedDataMapSet> getCarvedBlocks() {
        return this.carvedBlocks;
    }

    public boolean isCarved(BlockPos targetBlockPos) {
        return this.carvedBlocks.containsKey(targetBlockPos);
    }

    public boolean hasCarvedData() {
        return this.hasCarvedBlocks;
    }

    public boolean isPositionCarved(BlockPos targetBlockPos) {
        return this.hasCarvedBlocks && this.carvedBlocks.containsKey(targetBlockPos);
    }
}
