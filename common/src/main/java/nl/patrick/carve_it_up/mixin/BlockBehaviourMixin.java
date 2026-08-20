// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/BlockBehaviourMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvedItemHelper;
import nl.patrick.carve_it_up.carving.CarvingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into BlockBehaviour to override middle-click pick block for carved blocks.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    // NewStart Override middle-click pick block to return carved block item with attached data
    @Inject(
        method = "getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptGetCloneItemStack(LevelReader levelReader, BlockPos targetBlockPos, BlockState state, boolean includeData, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if (CarvingManager.isCarved(levelReader, targetBlockPos)) {
            CarvedData carvedData = CarvingManager.getCarvedData(levelReader, targetBlockPos);
            if (carvedData != null) {
                ItemStack carvedDrop = CarvedItemHelper.createCarvedBlockDrop(carvedData);
                if (!carvedDrop.isEmpty()) {
                    callbackInfoReturnable.setReturnValue(carvedDrop);
                }
            }
        }
    }
    // NewEnd
}
