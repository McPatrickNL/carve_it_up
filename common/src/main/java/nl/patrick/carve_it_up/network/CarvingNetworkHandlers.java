// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/network/CarvingNetworkHandlers.java
package nl.patrick.carve_it_up.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.CarvingModelFactory;
import nl.patrick.carve_it_up.carving.CarvingMode;
import nl.patrick.carve_it_up.item.CarvingToolItem;
import nl.patrick.carve_it_up.services.Services;

import java.util.HashMap;

/**
 * Server-side handling for Carve It Up network payloads. Deliberately kept free of any
 * client-only imports (Minecraft, BlockStateModel, etc.) since this class is referenced from the
 * platform mod init classes, which load on dedicated servers too.
 */
public class CarvingNetworkHandlers {

    private static final double MAX_CARVE_REACH_DISTANCE = 6.0;

    /**
     * Server-side handler for RequestCarveActionPayload. Re-validates every assumption the client
     * made before applying anything, since the client's request is untrusted input.
     */
    public static void handleCarveActionRequest(RequestCarveActionPayload payload, ServerPlayer player) {
        Level worldLevel = player.level();
        BlockPos targetPosition = payload.targetBlockPos();

        // 1. Tool still held?
        ItemStack heldItemStack = player.getMainHandItem();
        if (!(heldItemStack.getItem() instanceof CarvingToolItem)) {
            return;
        }

        // 2. Block still carved? If not carved, initialize on-the-fly from the original in-world shape
        if (!CarvingManager.isCarved(worldLevel, targetPosition)) {
            BlockState blockState = worldLevel.getBlockState(targetPosition);
            if (blockState.isAir() || !blockState.getFluidState().isEmpty() || blockState.getDestroySpeed(worldLevel, targetPosition) < 0.0F) {
                return;
            }
            CarvedData freshCarvedData = new CarvedData(
                blockState,
                player.getUUID(),
                16
            );
            CarvingModelFactory.populateFromShape(freshCarvedData, blockState, worldLevel, targetPosition);
            CarvingManager.setCarvedData(worldLevel, targetPosition, freshCarvedData);
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

        // 5. If the player used the material attached to their Carving Tool, consume it and auto-reload the next block from inventory
        if (payload.mode() == CarvingMode.ADD || payload.mode() == CarvingMode.REPLACE) {
            if (CarvingToolItem.hasLoadedMaterial(heldItemStack) && CarvingToolItem.getLoadedMaterial(heldItemStack) == payload.material().getBlock()) {
                Block consumedBlock = CarvingToolItem.getLoadedMaterial(heldItemStack);
                CarvingToolItem.clearLoadedMaterial(heldItemStack);

                if (!player.isCreative()) {
                    // Search player inventory for another block of the same type to automatically keep tool loaded
                    for (int slotIndex = 0; slotIndex < player.getInventory().getContainerSize(); slotIndex++) {
                        ItemStack inventoryStack = player.getInventory().getItem(slotIndex);
                        if (!inventoryStack.isEmpty() && inventoryStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == consumedBlock) {
                            inventoryStack.shrink(1);
                            CarvingToolItem.setLoadedMaterial(heldItemStack, consumedBlock);
                            break;
                        }
                    }
                } else {
                    // In creative mode, keep the loaded block continuously
                    CarvingToolItem.setLoadedMaterial(heldItemStack, consumedBlock);
                }
                player.inventoryMenu.broadcastChanges();
            }
        }

        // 6. When all voxels of a material are depleted from the carved block, pop it out into the world as an item
        for (Block depletedBlock : carvingResult.getDepletedMaterials()) {
            if (depletedBlock != null && depletedBlock != Blocks.AIR && depletedBlock != carvedData.getOriginalBlockState().getBlock()) {
                Block.popResource(worldLevel, targetPosition, new ItemStack(depletedBlock));
            }
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

    /**
     * Server-side handler for changing the base material of a carved block on right-click in Material mode.
     */
    public static void handleChangeBaseMaterialRequest(RequestChangeBaseMaterialPayload payload, ServerPlayer player) {
        Level worldLevel = player.level();
        BlockPos targetPosition = payload.targetBlockPos();
        BlockState newMaterialState = payload.newMaterial();
        if (newMaterialState == null || newMaterialState.isAir()) {
            return;
        }
        Block newMaterialBlock = newMaterialState.getBlock();

        // 1. Tool still held?
        ItemStack heldItemStack = player.getMainHandItem();
        if (!(heldItemStack.getItem() instanceof CarvingToolItem)) {
            return;
        }

        // 2. Lock not held by someone else?
        if (CarvingManager.isLockedBySomeoneElse(targetPosition, player)) {
            return;
        }

        // 3. Within reach?
        double distanceSquared = player.distanceToSqr(targetPosition.getX() + 0.5, targetPosition.getY() + 0.5, targetPosition.getZ() + 0.5);
        if (distanceSquared > MAX_CARVE_REACH_DISTANCE * MAX_CARVE_REACH_DISTANCE) {
            return;
        }

        // 4. Ensure carved data exists
        if (!CarvingManager.isCarved(worldLevel, targetPosition)) {
            BlockState blockState = worldLevel.getBlockState(targetPosition);
            if (blockState.isAir() || !blockState.getFluidState().isEmpty() || blockState.getDestroySpeed(worldLevel, targetPosition) < 0.0F) {
                return;
            }
            CarvedData freshCarvedData = new CarvedData(
                blockState,
                player.getUUID(),
                16
            );
            CarvingModelFactory.populateFromShape(freshCarvedData, blockState, worldLevel, targetPosition);
            CarvingManager.setCarvedData(worldLevel, targetPosition, freshCarvedData);
        }

        CarvedData carvedData = CarvingManager.getCarvedData(worldLevel, targetPosition);
        if (carvedData == null) {
            return;
        }

        BlockState oldBaseState = carvedData.getOriginalBlockState();
        Block oldBaseBlock = oldBaseState.getBlock();
        if (oldBaseBlock == newMaterialBlock) {
            return; // Already the same base material
        }

        // 5. Consume 1 item of new material in Survival mode if available
        if (!player.isCreative()) {
            boolean consumed = false;
            if (CarvingToolItem.hasLoadedMaterial(heldItemStack) && CarvingToolItem.getLoadedMaterial(heldItemStack) == newMaterialBlock) {
                CarvingToolItem.clearLoadedMaterial(heldItemStack);
                consumed = true;
                // Auto-reload from inventory
                for (int slotIndex = 0; slotIndex < player.getInventory().getContainerSize(); slotIndex++) {
                    ItemStack inventoryStack = player.getInventory().getItem(slotIndex);
                    if (!inventoryStack.isEmpty() && inventoryStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == newMaterialBlock) {
                        inventoryStack.shrink(1);
                        CarvingToolItem.setLoadedMaterial(heldItemStack, newMaterialBlock);
                        break;
                    }
                }
            } else {
                for (int slotIndex = 0; slotIndex < player.getInventory().getContainerSize(); slotIndex++) {
                    ItemStack inventoryStack = player.getInventory().getItem(slotIndex);
                    if (!inventoryStack.isEmpty() && inventoryStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == newMaterialBlock) {
                        inventoryStack.shrink(1);
                        consumed = true;
                        break;
                    }
                }
            }
            if (!consumed) {
                return; // Player doesn't possess the new material block
            }
            player.inventoryMenu.broadcastChanges();
        }

        // 6. Update base material in CarvedData while preserving all existing carved voxel states
        carvedData.setOriginalBlockState(newMaterialState);

        // 7. Update in-world block state
        worldLevel.setBlock(targetPosition, newMaterialState, 3);
        CarvingManager.setCarvedData(worldLevel, targetPosition, carvedData);

        // 8. Check if any voxels still use old base block. If not, eject it as a drop!
        boolean oldBaseUsed = false;
        for (BlockState voxelState : carvedData.getVoxelMaterials().values()) {
            if (voxelState != null && voxelState.getBlock() == oldBaseBlock) {
                oldBaseUsed = true;
                break;
            }
        }
        if (!oldBaseUsed) {
            Block.popResource(worldLevel, targetPosition, new ItemStack(oldBaseBlock));
            carvedData.getBlocks().remove(oldBaseBlock);
        }

        carvedData.rebuildBlockPalette();
        carvedData.incrementVersion();

        // 9. Play placement sound of new material
        worldLevel.playSound(null, targetPosition, newMaterialState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

        // 10. Broadcast updated carved data to all tracking clients
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