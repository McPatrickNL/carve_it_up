// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/CarvingNetworkHandlers.java
package nl.patrick.carve_it_up.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.CarvingModelFactory;
import nl.patrick.carve_it_up.item.CarvingToolItem;
import nl.patrick.carve_it_up.services.Services;

import java.util.HashMap;

/**
 * Server-side handling for Carve It Up network payloads. Deliberately kept free of any
 * client-only imports (Minecraft, BlockStateModel, etc.) since this class is referenced from the
 * platform mod init classes, which load on dedicated servers too.
 */
public class CarvingNetworkHandlers { // Converted from Allman style brace

    // Review Replace with the player's real reach attribute once confirmed against the 26.1 mappings
    private static final double MAX_CARVE_REACH_DISTANCE = 6.0;

    /**
     * Server-side handler for RequestCarveActionPayload. Re-validates every assumption the client
     * made before applying anything, since the client's request is untrusted input.
     *
     * @param payload The carve action payload sent by the client
     * @param player The server player sending the request
     */
    public static void handleCarveActionRequest(RequestCarveActionPayload payload, ServerPlayer player) {
        Level worldLevel = player.level();
        BlockPos targetPosition = payload.targetBlockPos();

        // 1. Tool still held?
        ItemStack heldItemStack = player.getMainHandItem();
        if (!(heldItemStack.getItem() instanceof CarvingToolItem)) {
            return;
        }

        // 2. Block still carved?
        if (!CarvingManager.isCarved(worldLevel, targetPosition)) {
            return;
        }

        // 3. Lock not held by someone else?
        if (CarvingManager.isLockedBySomeoneElse(targetPosition, player)) {
            return;
        }

        // 4. Within reach?
        double distanceSquared = player.distanceToSqr(targetPosition.getX() + 0.5, targetPosition.getY() + 0.5, targetPosition.getZ() + 0.5);
        if (distanceSquared > MAX_CARVE_REACH_DISTANCE * MAX_CARVE_REACH_DISTANCE) {
            return;
        }

        CarvedData carvedData = CarvingManager.getCarvedData(worldLevel, targetPosition);
        if (carvedData == null) {
            return;
        }

        // Apply the carve action server-authoritatively
        CarvingModelFactory.CarvingResult carvingResult = CarvingModelFactory.applyCarvingAction(
            carvedData,
            payload.mode(),
            payload.pattern(),
            payload.voxelX(), payload.voxelY(), payload.voxelZ(),
            payload.material(),
            payload.width(),
            payload.direction(),
            payload.face()
        );

        // Nothing actually changed — skip the block update + broadcast entirely
        if (carvingResult.getVoxelsModified() <= 0) {
            return;
        }

        BlockState currentBlockState = worldLevel.getBlockState(targetPosition);
        worldLevel.sendBlockUpdated(targetPosition, currentBlockState, currentBlockState, 3);

        // Broadcast the updated voxel snapshot to every tracking client
        SyncCarvedDataPayload syncPayload = new SyncCarvedDataPayload(
            targetPosition,
            carvedData.getOriginalBlockState(),
            carvedData.getOwnerUuid(),
            carvedData.getResolution(),
            carvedData.getVersion(),
            new HashMap<>(carvedData.getVoxelMaterials())
        );
        Services.NETWORK.sendToTrackingClients(worldLevel, targetPosition, syncPayload);
    }
}