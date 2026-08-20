// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/BlockItemMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedItemHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin onto BlockItem to restore carved block voxel structures when placing an ItemStack with carved data.
 */
@Mixin(BlockItem.class)
public class BlockItemMixin {

    // NewStart Restore carved block data immediately when the block is set in the world, before the itemstack is shrunk
    @Inject(
        method = "placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z",
        at = @At("RETURN")
    )
    private void onPlaceBlock(BlockPlaceContext placeContext, BlockState blockState, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (callbackInfoReturnable.getReturnValue()) {
            Level worldLevel = placeContext.getLevel();
            BlockPos placedBlockPos = placeContext.getClickedPos();
            ItemStack itemInHand = placeContext.getItemInHand();

            if (CarvedItemHelper.hasCarvedData(itemInHand)) {
                CarvedItemHelper.applyCarvedDataFromItem(worldLevel, placedBlockPos, itemInHand);
            }
        }
    }
    // NewEnd
}
