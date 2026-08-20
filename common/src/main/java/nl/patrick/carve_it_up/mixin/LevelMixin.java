// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/LevelMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvedItemHelper;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;
import nl.patrick.carve_it_up.services.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mixin onto Level to detect when a carved block is broken or replaced,
 * drop the custom item with attached voxel data, clean up chunk data on break,
 * and adaptively update voxel textures when blocks spread (e.g. grass spreading to dirt).
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    // NewStart Handle block breaking vs block state transitions (e.g. grass spreading)
    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD")
    )
    private void onSetBlock(BlockPos targetBlockPos, BlockState newBlockState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Level worldLevel = (Level)(Object) this;
        if (CarvingManager.isCarved(worldLevel, targetBlockPos)) {
            CarvedData currentCarvedData = CarvingManager.getCarvedData(worldLevel, targetBlockPos);
            if (currentCarvedData != null) {

                // 1. BLOCK BROKEN TO AIR: drop custom carved item with data and remove container
                if (newBlockState.isAir()) {
                    if (!worldLevel.isClientSide()) {
                        ItemStack dropStack = CarvedItemHelper.createCarvedBlockDrop(currentCarvedData);
                        if (!dropStack.isEmpty()) {
                            Block.popResource(worldLevel, targetBlockPos, dropStack);
                        }
                    }

                    CarvingManager.removeCarvedData(worldLevel, targetBlockPos);

                    if (!worldLevel.isClientSide()) {
                        SyncCarvedDataPayload removalPayload = new SyncCarvedDataPayload(
                            targetBlockPos,
                            newBlockState,
                            currentCarvedData.getOwnerUuid(),
                            currentCarvedData.getResolution(),
                            currentCarvedData.getVersion() + 1,
                            Collections.emptyMap()
                        );
                        Services.NETWORK.sendToTrackingClients(worldLevel, targetBlockPos, removalPayload);
                    } else {
                        ClientCarvingCache.invalidate(targetBlockPos);
                    }
                }
                // 2. BLOCK STATE SPREAD / IN-PLACE CONVERSION (e.g. grass spreading to dirt): update base voxels smoothly
                else if (newBlockState.getBlock() != currentCarvedData.getOriginalBlockState().getBlock()) {
                    Block oldBaseBlock = currentCarvedData.getOriginalBlockState().getBlock();
                    for (Map.Entry<Integer, BlockState> voxelEntry : currentCarvedData.getVoxelMaterials().entrySet()) {
                        if (voxelEntry.getValue() != null && voxelEntry.getValue().getBlock() == oldBaseBlock) {
                            voxelEntry.setValue(newBlockState);
                        }
                    }

                    currentCarvedData.rebuildBlockPalette();
                    currentCarvedData.incrementVersion();

                    if (!worldLevel.isClientSide()) {
                        SyncCarvedDataPayload updatePayload = new SyncCarvedDataPayload(
                            targetBlockPos,
                            newBlockState,
                            currentCarvedData.getOwnerUuid(),
                            currentCarvedData.getResolution(),
                            currentCarvedData.getVersion(),
                            new HashMap<>(currentCarvedData.getVoxelMaterials())
                        );
                        Services.NETWORK.sendToTrackingClients(worldLevel, targetBlockPos, updatePayload);
                    } else {
                        ClientCarvingCache.invalidate(targetBlockPos);
                    }
                }
            }
        }
    }
    // NewEnd
}
