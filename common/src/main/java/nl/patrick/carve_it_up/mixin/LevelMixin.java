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

/**
 * Mixin onto Level to detect when a carved block is broken or replaced,
 * drop the custom item with attached voxel data, and clean up chunk data.
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    // NewStart Drop custom carved item and clean up carved data on block break/replacement
    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD")
    )
    private void onSetBlock(BlockPos targetBlockPos, BlockState newBlockState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Level worldLevel = (Level)(Object) this;
        if (CarvingManager.isCarved(worldLevel, targetBlockPos)) {
            CarvedData currentCarvedData = CarvingManager.getCarvedData(worldLevel, targetBlockPos);
            if (currentCarvedData != null) {
                // If the block is being removed (broken to air) or replaced with a different block type
                if (newBlockState.isAir() || newBlockState.getBlock() != currentCarvedData.getOriginalBlockState().getBlock()) {

                    // If broken on the server, drop the customized ItemStack containing all voxel data
                    if (!worldLevel.isClientSide() && newBlockState.isAir()) {
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
            }
        }
    }
    // NewEnd
}
