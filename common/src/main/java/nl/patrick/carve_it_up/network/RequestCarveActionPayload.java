// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/RequestCarveActionPayload.java
package nl.patrick.carve_it_up.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvingMode;
import nl.patrick.carve_it_up.carving.CarvingPattern;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Client-to-server payload requesting a voxel-level carve action on an already-carved block.
 * The server re-validates every field before calling CarvingModelFactory.applyCarvingAction().
 */
public record RequestCarveActionPayload(
    BlockPos targetBlockPos,
    CarvingMode mode,
    CarvingPattern pattern,
    int voxelX,
    int voxelY,
    int voxelZ,
    BlockState material,
    Direction direction,
    Direction face,
    int width
) implements CustomPacketPayload {
    public static final Type<RequestCarveActionPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "request_carve_action"));

    public static final StreamCodec<FriendlyByteBuf, RequestCarveActionPayload> STREAM_CODEC = StreamCodec.of(
        RequestCarveActionPayload::write,
        RequestCarveActionPayload::read
    );

    private static void write(FriendlyByteBuf buffer, RequestCarveActionPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.targetBlockPos());
        buffer.writeVarInt(payload.mode().ordinal());
        buffer.writeVarInt(payload.pattern().ordinal());
        buffer.writeVarInt(payload.voxelX());
        buffer.writeVarInt(payload.voxelY());
        buffer.writeVarInt(payload.voxelZ());
        // NewStart Serialize block state as VarInt registry ID to fix writeBlockState compilation error
        buffer.writeVarInt(Block.getId(payload.material()));
        // NewEnd
        Direction.STREAM_CODEC.encode(buffer, payload.direction());
        Direction.STREAM_CODEC.encode(buffer, payload.face());
        buffer.writeVarInt(payload.width());
    }

    private static RequestCarveActionPayload read(FriendlyByteBuf buffer) {
        BlockPos targetBlockPos = BlockPos.STREAM_CODEC.decode(buffer);
        CarvingMode mode = CarvingMode.values()[buffer.readVarInt()];
        CarvingPattern pattern = CarvingPattern.values()[buffer.readVarInt()];
        int voxelX = buffer.readVarInt();
        int voxelY = buffer.readVarInt();
        int voxelZ = buffer.readVarInt();
        // NewStart Deserialize block state from VarInt registry ID to fix readBlockState compilation error
        BlockState material = Block.stateById(buffer.readVarInt());
        // NewEnd
        Direction direction = Direction.STREAM_CODEC.decode(buffer);
        Direction face = Direction.STREAM_CODEC.decode(buffer);
        int width = buffer.readVarInt();
        return new RequestCarveActionPayload(targetBlockPos, mode, pattern, voxelX, voxelY, voxelZ, material, direction, face, width);
    }

    @Override
    public Type<RequestCarveActionPayload> type() {
        return TYPE;
    }
}