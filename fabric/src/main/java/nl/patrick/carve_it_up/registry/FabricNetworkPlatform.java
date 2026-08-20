// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricNetworkPlatform.java
package nl.patrick.carve_it_up.registry;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import nl.patrick.carve_it_up.services.INetworkPlatform;

public class FabricNetworkPlatform implements INetworkPlatform {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        // NewStart Send payload from client to server using Fabric ClientPlayNetworking
        ClientPlayNetworking.send(payload);
        // NewEnd
    }

    @Override
    public void sendToTrackingClients(Level level, BlockPos pos, CustomPacketPayload payload) {
        // Guard: only meaningful on the logical server; silently no-ops on the client so callers
        // (like CarvingToolItem.useOn()) don't need to check the side themselves.
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : PlayerLookup.tracking(serverLevel, pos)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}