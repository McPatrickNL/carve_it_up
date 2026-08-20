// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/ClientCarvingNetworkHandlers.java
package nl.patrick.carve_it_up.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;

/**
 * Client-side handling for SyncCarvedDataPayload. Writes authoritative server snapshots
 * into client chunk data and invalidates caches and chunk sections for re-tesselation.
 */
public class ClientCarvingNetworkHandlers { // Converted from Allman style brace

    /**
     * Client-side handler for SyncCarvedDataPayload. Writes the server's authoritative voxel
     * snapshot into the client's own ChunkCarvedData tree and forces chunk section re-tesselation.
     *
     * @param payload The carved voxel data payload sent from the server
     */
    public static void handleSyncCarvedData(SyncCarvedDataPayload payload) {
        Minecraft minecraftInstance = Minecraft.getInstance();
        ClientLevel clientLevel = minecraftInstance.level;
        if (clientLevel == null) {
            return;
        }

        BlockPos targetBlockPos = payload.blockPos();
        ChunkCarvedData chunkCarvedData = ((IChunkCarvedDataAccessor) clientLevel.getChunkAt(targetBlockPos)).carveitup$getCarvedData();
        CarvedData existingCarvedData = chunkCarvedData.getCarvedData(targetBlockPos);

        if (existingCarvedData == null) {
            CarvedData freshCarvedData = new CarvedData(
                payload.originalBlockState(),
                clientLevel,
                targetBlockPos,
                CollisionContext.empty(),
                payload.ownerUuid(),
                payload.resolution()
            );
            freshCarvedData.getVoxelMaterials().clear();
            freshCarvedData.getVoxelMaterials().putAll(payload.voxelMaterials());
            while (freshCarvedData.getVersion() < payload.version()) {
                freshCarvedData.incrementVersion();
            }
            chunkCarvedData.addCarvedData(clientLevel, targetBlockPos, freshCarvedData);
        } else {
            existingCarvedData.getVoxelMaterials().clear();
            existingCarvedData.getVoxelMaterials().putAll(payload.voxelMaterials());
            while (existingCarvedData.getVersion() < payload.version()) {
                existingCarvedData.incrementVersion();
            }
        }

        // Invalidate model cache entry for this position
        ClientCarvingCache.invalidate(targetBlockPos);

        // NewStart Force immediate chunk section re-render and neighbor update
        if (minecraftInstance.levelRenderer != null) {
            int sectionCoordinateX = SectionPos.blockToSectionCoord(targetBlockPos.getX());
            int sectionCoordinateY = SectionPos.blockToSectionCoord(targetBlockPos.getY());
            int sectionCoordinateZ = SectionPos.blockToSectionCoord(targetBlockPos.getZ());
            minecraftInstance.levelRenderer.setSectionDirtyWithNeighbors(sectionCoordinateX, sectionCoordinateY, sectionCoordinateZ);
        }
        // NewEnd
    }
}