// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeNetworkPlatform.java
package nl.patrick.carve_it_up.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import nl.patrick.carve_it_up.services.INetworkPlatform;

public class NeoForgeNetworkPlatform implements INetworkPlatform {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        // NewStart Send client-to-server payload via Minecraft client connection
        Minecraft minecraftInstance = Minecraft.getInstance();
        if (minecraftInstance.getConnection() != null) {
            minecraftInstance.getConnection().send(new ServerboundCustomPayloadPacket(payload));
        }
        // NewEnd
    }

    @Override
    public void sendToTrackingClients(Level level, BlockPos pos, CustomPacketPayload payload) {
        // Guard: only meaningful on the logical server; silently no-ops on the client.
        if (level instanceof ServerLevel serverLevel) {
            // NewStart Send payload to tracking clients using ChunkPos.containing
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(pos), payload);
            // NewEnd
        }
    }
}