// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/SyncCarvedDataPayload.java
package nl.patrick.carve_it_up.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Server-to-client payload carrying a full snapshot of one block's carved voxel data.
 */
public record SyncCarvedDataPayload(
    BlockPos blockPos,
    BlockState originalBlockState,
    UUID ownerUuid,
    int resolution,
    int version,
    Map<Integer, BlockState> voxelMaterials
) implements CustomPacketPayload {

    public static final Type<SyncCarvedDataPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_carved_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncCarvedDataPayload> STREAM_CODEC = StreamCodec.of(
        SyncCarvedDataPayload::write,
        SyncCarvedDataPayload::read
    );

    private static void write(FriendlyByteBuf buffer, SyncCarvedDataPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.blockPos());
        buffer.writeVarInt(Block.getId(payload.originalBlockState()));
        buffer.writeUUID(payload.ownerUuid());
        buffer.writeVarInt(payload.resolution());
        buffer.writeVarInt(payload.version());

        Map<Integer, BlockState> voxelMaterials = payload.voxelMaterials();
        buffer.writeVarInt(voxelMaterials.size());
        for (Map.Entry<Integer, BlockState> entry : voxelMaterials.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeVarInt(Block.getId(entry.getValue()));
        }
    }

    private static SyncCarvedDataPayload read(FriendlyByteBuf buffer) {
        BlockPos blockPos = BlockPos.STREAM_CODEC.decode(buffer);
        BlockState originalBlockState = Block.stateById(buffer.readVarInt());
        UUID ownerUuid = buffer.readUUID();
        int resolution = buffer.readVarInt();
        int version = buffer.readVarInt();

        int voxelCount = buffer.readVarInt();
        Map<Integer, BlockState> voxelMaterials = new HashMap<>();
        for (int iterationIndex = 0; iterationIndex < voxelCount; iterationIndex++) {
            int voxelIndex = buffer.readVarInt();
            BlockState state = Block.stateById(buffer.readVarInt());
            voxelMaterials.put(voxelIndex, state);
        }

        return new SyncCarvedDataPayload(blockPos, originalBlockState, ownerUuid, resolution, version, voxelMaterials);
    }

    @Override
    public Type<SyncCarvedDataPayload> type() {
        return TYPE;
    }
}