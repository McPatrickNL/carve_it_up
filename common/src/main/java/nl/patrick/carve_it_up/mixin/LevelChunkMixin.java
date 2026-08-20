// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/LevelChunkMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import nl.patrick.carve_it_up.carving.CarvedDataMapSet;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mixin onto LevelChunk to transfer carved block data when graduating a ProtoChunk to a full LevelChunk.
 */
@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    // NewStart Inherit carved data when constructing a full LevelChunk from a ProtoChunk
    @Inject(
        method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
        at = @At("RETURN")
    )
    private void onConstructFromProtoChunk(ServerLevel serverLevel, ProtoChunk protoChunk, LevelChunk.PostLoadProcessor postLoadProcessor, CallbackInfo callbackInfo) {
        if (protoChunk instanceof IChunkCarvedDataAccessor protoAccessor && (Object) this instanceof IChunkCarvedDataAccessor levelChunkAccessor) {
            ChunkCarvedData protoData = protoAccessor.carveitup$getCarvedData();
            ChunkCarvedData levelChunkData = levelChunkAccessor.carveitup$getCarvedData();
            if (protoData != null && protoData.hasCarvedData()) {
                for (Map.Entry<BlockPos, CarvedDataMapSet> entry : protoData.getCarvedBlocks().entrySet()) {
                    levelChunkData.addCarvedData(serverLevel, entry.getKey(), entry.getValue().getCarvedData());
                }
            }
        }
    }
    // NewEnd
}
