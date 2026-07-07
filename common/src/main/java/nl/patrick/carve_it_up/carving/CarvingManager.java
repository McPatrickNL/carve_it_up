package nl.patrick.carve_it_up.carving;

//import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;
import java.util.WeakHashMap;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingManager.java

public class CarvingManager
{
    // Tracks chunks that have active carvings.
    // WeakHashMap automatically drops references when chunks unload, preventing memory leaks!
    private static final Map<LevelChunk, Boolean> TRACKED_CHUNKS = new WeakHashMap<>();
    
    /**
     * Light-weight, high-performance check to see if a coordinate has any carving data.
     * Perfect for rendering and physics hot-paths.
     */
    public static boolean isCarved(BlockGetter level, BlockPos pos) {
//        LOGGER.info("Asking if this block is carved " + pos.toShortString());
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);// If the entire chunk contains zero carvings, exit immediately!
        if (chunkData == null || !chunkData.hasCarvedData()) {
            return false;
        }
        return chunkData.hasCarvedData() && chunkData.isCarved(pos);
    }
    
    /**
     * Grabs the full structural CarvedData container for a position, if it exists.
     */
    public static CarvedData getCarvedData(BlockGetter level, BlockPos pos) {
//        LOGGER.info("Requesting carved block data from " + pos.toShortString());
        if (isCarved(level, pos)) {
            ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
            if (chunkData != null) {
                return chunkData.getCarvedData(pos);
            }
        }
        return null;
    }
    
    /**
     * Shared internal bridge to safely locate the ChunkCarvedData capsule across environments.
     */
    private static ChunkCarvedData getChunkCarvedData(BlockGetter level, BlockPos pos) {
//        LOGGER.info("Requesting carved chunk data from " + pos.toShortString());
        if (level instanceof Level world) {
            var chunk = world.getChunkAt(pos);
            return ((IChunkCarvedDataAccessor) chunk).carveitup$getCarvedData();
        }
        return null;
    }
    
    /**
     * Assigns custom CarvedData to a specific position.
     */
    public static void setCarvedData(Level level, BlockPos pos, CarvedData data) {
//        LOGGER.info("Requesting carved block data to be added to " + pos.toShortString());
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
        if (chunkData != null) {
            chunkData.addCarvedData(level, pos, data);
            
            // Mark the chunk as modified so the server knows it changed
            if (level instanceof Level world && !world.isClientSide()) {
                world.getChunkAt(pos).markUnsaved();
            }
        }
    }
    
    /**
     * Removes carving data from a specific position.
     */
    public static void removeCarvedData(BlockGetter level, BlockPos pos) {
//        LOGGER.info("Requesting carved block data from " + pos.toShortString());
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
        if (chunkData != null) {
            chunkData.removeCarvedData(pos);
            
            if (level instanceof Level world && !world.isClientSide()) {
                world.getChunkAt(pos).markUnsaved();
            }
        }
    }
    
    /**
     * Flat out wipes all carving data from currently tracked loaded chunks.
     * Call this for rapid testing without restarting your game client!
     * High-performance, clean, and safe from 26.1 refactor breaking points.
     */
    public static void debugWipeAllLoadedData() {
//        LOGGER.info("request to wipe all data");
        for (LevelChunk chunk : TRACKED_CHUNKS.keySet()) {
            if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                ChunkCarvedData chunkData = accessor.carveitup$getCarvedData();
                Map<BlockPos, CarvedDataMapSet> carvedBlocks = chunkData.getCarvedBlocks();
                
                // Loop through all marked Blocks in this chunk
                for (Map.Entry<BlockPos, CarvedDataMapSet> dataEntry : carvedBlocks.entrySet())
                {
                    removeCarvedDataLight(dataEntry.getValue().getLevel(), dataEntry.getKey());
                }
                chunk.markUnsaved();
            }
        }
        TRACKED_CHUNKS.clear();
        LOGGER.info("All carved data wiped");
    }
    
    /**
     * Removes carving data from a specific position.
     */
    private static void removeCarvedDataLight(BlockGetter level, BlockPos pos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
        if (chunkData != null) {
            chunkData.removeCarvedData(pos);
            
            if (level instanceof Level world && !world.isClientSide()) {
                world.getChunkAt(pos).markUnsaved();
            }
        }
    }
}
