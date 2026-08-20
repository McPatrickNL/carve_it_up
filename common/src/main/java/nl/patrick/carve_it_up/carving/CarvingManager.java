// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingManager.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;

/**
 * Global manager handling retrieval, mutation, locking, and tracking of carved block data.
 */
public class CarvingManager {

    private static final Map<LevelChunk, Boolean> TRACKED_CHUNKS = new WeakHashMap<>();
    private static final Map<BlockPos, UUID> ACTIVE_LOCKS = new ConcurrentHashMap<>();

    /**
     * Light-weight, high-performance check to see if a coordinate has any carving data.
     * Perfect for rendering and physics hot-paths.
     *
     * @param level The block getter or level instance
     * @param targetBlockPos The position to check
     * @return True if the block is carved
     */
    public static boolean isCarved(BlockGetter level, BlockPos targetBlockPos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, targetBlockPos);
        if (chunkData == null || !chunkData.hasCarvedData()) {
            return false;
        }
        return chunkData.isPositionCarved(targetBlockPos);
    }

    public static boolean isLockedBySomeoneElse(BlockPos targetBlockPos, Player player) {
        UUID lockOwnerUuid = ACTIVE_LOCKS.get(targetBlockPos);
        return lockOwnerUuid != null && !lockOwnerUuid.equals(player.getUUID());
    }

    /**
     * Grabs the full structural CarvedData container for a position, if it exists.
     *
     * @param level The block getter or level instance
     * @param targetBlockPos The position to query
     * @return The CarvedData or null if not carved
     */
    public static CarvedData getCarvedData(BlockGetter level, BlockPos targetBlockPos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, targetBlockPos);
        if (chunkData != null && chunkData.hasCarvedData()) {
            return chunkData.getCarvedData(targetBlockPos);
        }
        return null;
    }

    /**
     * Shared internal bridge to safely locate ChunkCarvedData across environments without thread deadlocks.
     */
    public static ChunkCarvedData getChunkCarvedData(BlockGetter level, BlockPos targetBlockPos) {
        if (level == null || targetBlockPos == null) {
            return null;
        }

        int chunkX = targetBlockPos.getX() >> 4;
        int chunkZ = targetBlockPos.getZ() >> 4;

        if (level instanceof Level worldLevelInstance) {
            ChunkAccess chunk = worldLevelInstance.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                return accessor.carveitup$getCarvedData();
            }
        } else if (level instanceof CollisionGetter collisionGetter) {
            BlockGetter chunk = collisionGetter.getChunkForCollisions(chunkX, chunkZ);
            if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                return accessor.carveitup$getCarvedData();
            }
        } else if (level instanceof LevelReader levelReader) {
            ChunkAccess chunk = levelReader.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                return accessor.carveitup$getCarvedData();
            }
        }

        // Safe fallback for client side
        try {
            Minecraft minecraftInstance = Minecraft.getInstance();
            if (minecraftInstance.level != null) {
                ChunkAccess chunk = minecraftInstance.level.getChunkSource().getChunk(chunkX, chunkZ, false);
                if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                    return accessor.carveitup$getCarvedData();
                }
            }
        } catch (Throwable ignored) {
            // Server-safe safeguard
        }

        return null;
    }

    /**
     * Assigns custom CarvedData to a specific position.
     *
     * @param worldLevel The active world level
     * @param targetBlockPos The block position
     * @param carvedData The CarvedData structure to assign
     */
    public static void setCarvedData(Level worldLevel, BlockPos targetBlockPos, CarvedData carvedData) {
        ChunkCarvedData chunkData = getChunkCarvedData(worldLevel, targetBlockPos);
        if (chunkData != null) {
            chunkData.addCarvedData(worldLevel, targetBlockPos, carvedData);

            if (!worldLevel.isClientSide()) {
                worldLevel.getChunkAt(targetBlockPos).markUnsaved();
            }
        }
    }

    /**
     * Alias for setCarvedData.
     */
    public static void addCarvedData(Level worldLevel, BlockPos targetBlockPos, CarvedData carvedData) {
        setCarvedData(worldLevel, targetBlockPos, carvedData);
    }

    /**
     * Removes carving data from a specific position.
     *
     * @param level The active level
     * @param targetBlockPos The block position to clear
     */
    public static void removeCarvedData(BlockGetter level, BlockPos targetBlockPos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, targetBlockPos);
        if (chunkData != null) {
            chunkData.removeCarvedData(targetBlockPos);

            if (level instanceof Level worldLevel && !worldLevel.isClientSide()) {
                worldLevel.getChunkAt(targetBlockPos).markUnsaved();
            }
        }
    }

    /**
     * Wipes all carving data from currently tracked loaded chunks for rapid testing.
     */
    public static void debugWipeAllLoadedData() {
        for (LevelChunk chunk : TRACKED_CHUNKS.keySet()) {
            if (chunk instanceof IChunkCarvedDataAccessor accessor) {
                ChunkCarvedData chunkData = accessor.carveitup$getCarvedData();
                Map<BlockPos, CarvedDataMapSet> carvedBlocks = chunkData.getCarvedBlocks();

                for (Map.Entry<BlockPos, CarvedDataMapSet> dataEntry : carvedBlocks.entrySet()) {
                    removeCarvedDataLight(dataEntry.getValue().getLevel(), dataEntry.getKey());
                }
                chunk.markUnsaved();
            }
        }
        TRACKED_CHUNKS.clear();
        LOGGER.info("All carved data wiped");
    }

    private static void removeCarvedDataLight(BlockGetter level, BlockPos targetBlockPos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, targetBlockPos);
        if (chunkData != null) {
            chunkData.removeCarvedData(targetBlockPos);

            if (level instanceof Level worldLevel && !worldLevel.isClientSide()) {
                worldLevel.getChunkAt(targetBlockPos).markUnsaved();
            }
        }
    }
}
