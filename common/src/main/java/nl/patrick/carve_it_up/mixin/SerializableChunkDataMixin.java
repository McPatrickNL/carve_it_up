// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/SerializableChunkDataMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.ChunkCarvedDataSerializer;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;
import nl.patrick.carve_it_up.carving.ISerializableChunkDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin onto SerializableChunkData to persist carved block structures into chunk NBT and load them back.
 */
@Mixin(SerializableChunkData.class)
public class SerializableChunkDataMixin implements ISerializableChunkDataAccessor {

    @Unique
    private CompoundTag carveitup$carvedDataTag;

    @Override
    public CompoundTag carveitup$getCarvedDataTag() {
        return this.carveitup$carvedDataTag;
    }

    @Override
    public void carveitup$setCarvedDataTag(CompoundTag tag) {
        this.carveitup$carvedDataTag = tag;
    }

    // NewStart Capture carved data from ChunkAccess during chunk copy for saving
    @Inject(
        method = "copyOf(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/world/level/chunk/storage/SerializableChunkData;",
        at = @At("RETURN")
    )
    private static void onCopyOf(ServerLevel serverLevel, ChunkAccess chunkAccess, CallbackInfoReturnable<SerializableChunkData> callbackInfoReturnable) {
        SerializableChunkData serializedData = callbackInfoReturnable.getReturnValue();
        if ((Object) serializedData instanceof ISerializableChunkDataAccessor dataAccessor) {
            ChunkAccess sourceChunk = chunkAccess;
            if (chunkAccess instanceof ImposterProtoChunk imposter) {
                sourceChunk = imposter.getWrapped();
            }
            if (sourceChunk instanceof IChunkCarvedDataAccessor chunkAccessor) {
                ChunkCarvedData chunkCarvedData = chunkAccessor.carveitup$getCarvedData();
                if (chunkCarvedData != null && chunkCarvedData.hasCarvedData()) {
                    dataAccessor.carveitup$setCarvedDataTag(ChunkCarvedDataSerializer.saveToTag(chunkCarvedData));
                }
            }
        }
    }
    // NewEnd

    // NewStart Write carved data NBT tag into output chunk compound
    @Inject(
        method = "write()Lnet/minecraft/nbt/CompoundTag;",
        at = @At("RETURN")
    )
    private void onWrite(CallbackInfoReturnable<CompoundTag> callbackInfoReturnable) {
        CompoundTag outputTag = callbackInfoReturnable.getReturnValue();
        if (this.carveitup$carvedDataTag != null && !this.carveitup$carvedDataTag.isEmpty()) {
            outputTag.put(ChunkCarvedDataSerializer.NBT_ROOT_KEY, this.carveitup$carvedDataTag);
        }
    }
    // NewEnd

    // NewStart Parse carved data NBT tag when reading chunk from disk
    @Inject(
        method = "parse(Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/chunk/PalettedContainerFactory;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/chunk/storage/SerializableChunkData;",
        at = @At("RETURN")
    )
    private static void onParse(LevelHeightAccessor heightAccessor, PalettedContainerFactory factory, CompoundTag inputTag, CallbackInfoReturnable<SerializableChunkData> callbackInfoReturnable) {
        SerializableChunkData parsedData = callbackInfoReturnable.getReturnValue();
        if ((Object) parsedData instanceof ISerializableChunkDataAccessor dataAccessor && inputTag.contains(ChunkCarvedDataSerializer.NBT_ROOT_KEY)) {
            dataAccessor.carveitup$setCarvedDataTag(inputTag.getCompoundOrEmpty(ChunkCarvedDataSerializer.NBT_ROOT_KEY));
        }
    }
    // NewEnd

    // NewStart Restore carved data into the materialized LevelChunk/ProtoChunk
    @Inject(
        method = "read(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/chunk/ProtoChunk;",
        at = @At("RETURN")
    )
    private void onRead(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo storageInfo, ChunkPos chunkPos, CallbackInfoReturnable<ProtoChunk> callbackInfoReturnable) {
        ProtoChunk protoChunk = callbackInfoReturnable.getReturnValue();
        if (protoChunk != null && this.carveitup$carvedDataTag != null && !this.carveitup$carvedDataTag.isEmpty()) {
            ChunkAccess targetChunk = protoChunk;
            if (protoChunk instanceof ImposterProtoChunk imposter) {
                targetChunk = imposter.getWrapped();
            }
            if (targetChunk instanceof IChunkCarvedDataAccessor chunkAccessor) {
                ChunkCarvedDataSerializer.loadFromTag(chunkAccessor.carveitup$getCarvedData(), serverLevel, this.carveitup$carvedDataTag);
            }
        }
    }
    // NewEnd
}
