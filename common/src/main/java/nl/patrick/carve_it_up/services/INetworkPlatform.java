package nl.patrick.carve_it_up.services;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/services/INetworkPlatform.java

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;


/**
 * Platform-agnostic entry point for sending Carve It Up network payloads. Fabric and NeoForge
 * each provide their own implementation, resolved at runtime through the ServiceLoader
 * (see Services.NETWORK).
 */
public interface INetworkPlatform
{
    /**
     * Sends a payload from the logical client to the logical server.
     */
    void sendToServer(CustomPacketPayload payload);
    
    /**
     * Sends a payload from the logical server to every client currently tracking the given
     * position. No-ops safely if level isn't actually a ServerLevel, so callers don't need to
     * guard the call themselves.
     */
    void sendToTrackingClients(Level level, BlockPos pos, CustomPacketPayload payload);
}