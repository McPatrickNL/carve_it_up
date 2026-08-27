// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/RequestPasteCarvingDataPayload.java
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

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Client-to-server payload requesting to paste a copied 3D voxel grid onto a targeted block.
 */
public record RequestPasteCarvingDataPayload(
    BlockPos targetBlockPos,
    int resolution,
    Map<Integer, BlockState> voxelMaterials
) implements CustomPacketPayload {

    public static final Type<RequestPasteCarvingDataPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "request_paste_carving_data"));

    public static final StreamCodec<FriendlyByteBuf, RequestPasteCarvingDataPayload> STREAM_CODEC = StreamCodec.of(
        RequestPasteCarvingDataPayload::write,
        RequestPasteCarvingDataPayload::read
    );

    private static void write(FriendlyByteBuf buffer, RequestPasteCarvingDataPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.targetBlockPos());
        buffer.writeVarInt(payload.resolution());

        Map<Integer, BlockState> voxelMaterials = payload.voxelMaterials();
        buffer.writeVarInt(voxelMaterials.size());
        for (Map.Entry<Integer, BlockState> entry : voxelMaterials.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeVarInt(Block.getId(entry.getValue()));
        }
    }

    private static RequestPasteCarvingDataPayload read(FriendlyByteBuf buffer) {
        BlockPos targetBlockPos = BlockPos.STREAM_CODEC.decode(buffer);
        int resolution = buffer.readVarInt();
        int voxelCount = buffer.readVarInt();
        Map<Integer, BlockState> voxelMaterials = new HashMap<>();
        for (int i = 0; i < voxelCount; i++) {
            int voxelIndex = buffer.readVarInt();
            BlockState state = Block.stateById(buffer.readVarInt());
            voxelMaterials.put(voxelIndex, state);
        }
        return new RequestPasteCarvingDataPayload(targetBlockPos, resolution, voxelMaterials);
    }

    @Override
    public Type<RequestPasteCarvingDataPayload> type() {
        return TYPE;
    }
}
