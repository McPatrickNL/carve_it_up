package nl.patrick.carve_it_up.network;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/ClientCarvingNetworkHandlers.java

// Review Once splitEnvironmentSourceSets() / src/client/java is adopted (already flagged as a
// TODO for the HUD rendering code), move this class there — it is only safe today because it is
// exclusively referenced from client-gated registration code (FabricModClient, and the
// Dist.CLIENT-subscribed listener on NeoForge), never from FabricMod/NeoForgeMod directly.

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;

public class ClientCarvingNetworkHandlers
{
    /**
     * Client-side handler for SyncCarvedDataPayload. Writes the server's authoritative voxel
     * snapshot into the client's own ChunkCarvedData tree (client and server each keep their own
     * copy via IChunkCarvedDataAccessor — the client's copy is what actually renders) and
     * invalidates the baked-model cache so ModelBlockRendererMixin re-bakes on the next tesselation.
     */
    public static void handleSyncCarvedData(SyncCarvedDataPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        
        BlockPos pos = payload.blockPos();
        ChunkCarvedData chunkCarvedData = ((IChunkCarvedDataAccessor) level.getChunkAt(pos)).carveitup$getCarvedData();
        CarvedData existingData = chunkCarvedData.getCarvedData(pos);
        
        if (existingData == null) {
            // NewStart First time this client has seen carved data at this position (e.g. it just
            // started tracking the chunk) — build a fresh local shell from the synced snapshot.
            // CollisionContext.empty() is a fine throwaway here since bakeCustomModel() promptly
            // recomputes the real shapes from the voxel grid a few lines below.
            CarvedData freshData = new CarvedData(
                payload.originalBlockState(),
                level,
                pos,
                CollisionContext.empty(),
                payload.ownerUuid(),
                payload.resolution()
            );
            freshData.getVoxelMaterials().clear();
            freshData.getVoxelMaterials().putAll(payload.voxelMaterials());
            while (freshData.getVersion() < payload.version()) {
                freshData.incrementVersion();
            }
            chunkCarvedData.addCarvedData(level, pos, freshData);
            // NewEnd
        } else {
            // NewStart Overwrite the existing local voxel data with the authoritative server snapshot.
            existingData.getVoxelMaterials().clear();
            existingData.getVoxelMaterials().putAll(payload.voxelMaterials());
            while (existingData.getVersion() < payload.version()) {
                existingData.incrementVersion();
            }
            // NewEnd
        }
        
        // Force a re-bake on next render and nudge the renderer to notice the change.
        ClientCarvingCache.invalidate(pos);
        var state = level.getBlockState(pos);
        // Review Confirm this is the right trigger for a chunk section re-tesselation in 26.1 —
        // if carved blocks don't visually refresh immediately, look at
        // Minecraft.getInstance().levelRenderer's block-changed hook instead.
        level.sendBlockUpdated(pos, state, state, 3);
    }
}