// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/PlayerChunkSenderMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvedDataMapSet;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin onto PlayerChunkSender to send all carved block structures in a chunk directly to the connecting player.
 */
@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {

    // NewStart Send carved data directly to the player packet listener
    @Inject(
        method = "sendChunk(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;)V",
        at = @At("RETURN")
    )
    private static void onSendChunk(ServerGamePacketListenerImpl packetListener, ServerLevel serverLevel, LevelChunk levelChunk, CallbackInfo callbackInfo) {
        if (packetListener != null && levelChunk instanceof IChunkCarvedDataAccessor accessor) {
            ChunkCarvedData chunkCarvedData = accessor.carveitup$getCarvedData();
            if (chunkCarvedData != null && chunkCarvedData.hasCarvedData()) {
                for (Map.Entry<BlockPos, CarvedDataMapSet> entry : chunkCarvedData.getCarvedBlocks().entrySet()) {
                    BlockPos blockPos = entry.getKey();
                    CarvedData carvedData = entry.getValue().getCarvedData();
                    if (carvedData != null) {
                        SyncCarvedDataPayload syncPayload = new SyncCarvedDataPayload(
                            blockPos,
                            carvedData.getOriginalBlockState(),
                            carvedData.getOwnerUuid(),
                            carvedData.getResolution(),
                            carvedData.getVersion(),
                            new HashMap<>(carvedData.getVoxelMaterials())
                        );
                        packetListener.send(new ClientboundCustomPayloadPacket(syncPayload));
                    }
                }
            }
        }
    }
    // NewEnd
}
