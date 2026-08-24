// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/BlockBehaviourMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvedItemHelper;
import nl.patrick.carve_it_up.carving.CarvingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Mixin into BlockBehaviour to override middle-click pick block and block break drops for carved blocks.
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

    // NewStart Override block drops to drop ONLY the carved item with data instead of the vanilla block
    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptGetDrops(BlockState state, LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> callbackInfoReturnable) {
        Vec3 origin = builder.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin != null) {
            BlockPos targetBlockPos = BlockPos.containing(origin);
            ServerLevel serverLevel = builder.getLevel();
            if (CarvingManager.isCarved(serverLevel, targetBlockPos)) {
                CarvedData carvedData = CarvingManager.getCarvedData(serverLevel, targetBlockPos);
                if (carvedData != null) {
                    ItemStack carvedDrop = CarvedItemHelper.createCarvedBlockDrop(carvedData);
                    if (!carvedDrop.isEmpty()) {
                        callbackInfoReturnable.setReturnValue(Collections.singletonList(carvedDrop));
                    }
                }
            }
        }
    }
    // NewEnd
}
