// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/RequestChangeBaseMaterialPayload.java
package nl.patrick.carve_it_up.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Client-to-server payload requesting to change the base material of a carved block.
 */
public record RequestChangeBaseMaterialPayload(
    BlockPos targetBlockPos,
    BlockState newMaterial
) implements CustomPacketPayload {

    public static final Type<RequestChangeBaseMaterialPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "request_change_base_material"));

    public static final StreamCodec<FriendlyByteBuf, RequestChangeBaseMaterialPayload> STREAM_CODEC = StreamCodec.of(
        RequestChangeBaseMaterialPayload::write,
        RequestChangeBaseMaterialPayload::read
    );

    private static void write(FriendlyByteBuf buffer, RequestChangeBaseMaterialPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.targetBlockPos());
        buffer.writeVarInt(Block.getId(payload.newMaterial()));
    }

    private static RequestChangeBaseMaterialPayload read(FriendlyByteBuf buffer) {
        BlockPos targetBlockPos = BlockPos.STREAM_CODEC.decode(buffer);
        BlockState newMaterial = Block.stateById(buffer.readVarInt());
        return new RequestChangeBaseMaterialPayload(targetBlockPos, newMaterial);
    }

    @Override
    public Type<RequestChangeBaseMaterialPayload> type() {
        return TYPE;
    }
}
