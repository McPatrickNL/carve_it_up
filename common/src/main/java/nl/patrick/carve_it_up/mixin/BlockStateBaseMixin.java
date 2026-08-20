// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/BlockStateBaseMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into BlockBehaviour.BlockStateBase to override physical collision shapes,
 * hitbox selection outlines, visual shapes, and full-block collision checks for carved blocks.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin { // Converted from Allman style brace

    // 1. PHYSICAL COLLISION
    @Inject(
        method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptCollisionShape(BlockGetter levelGetter, BlockPos targetBlockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
        CarvedData blockCarvedData = CarvingManager.getCarvedData(levelGetter, targetBlockPos);
        if (blockCarvedData != null) {
            callbackInfoReturnable.setReturnValue(blockCarvedData.getCollisionShape());
        }
    }

    // 2. SELECTION WIREFRAME / HITBOX OUTLINE
    @Inject(
        method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptShape(BlockGetter levelGetter, BlockPos targetBlockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
        CarvedData blockCarvedData = CarvingManager.getCarvedData(levelGetter, targetBlockPos);
        if (blockCarvedData != null) {
            callbackInfoReturnable.setReturnValue(blockCarvedData.getCollisionShape());
        }
    }

    // 3. LIGHT / PATHFINDING VISUAL SHAPE
    @Inject(
        method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptVisualShape(BlockGetter levelGetter, BlockPos targetBlockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
        CarvedData blockCarvedData = CarvingManager.getCarvedData(levelGetter, targetBlockPos);
        if (blockCarvedData != null) {
            callbackInfoReturnable.setReturnValue(blockCarvedData.getVisualShape());
        }
    }

    // 4. INTERACTION SHAPE
    @Inject(
        method = "getInteractionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptInteractionShape(BlockGetter levelGetter, BlockPos targetBlockPos, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
        CarvedData blockCarvedData = CarvingManager.getCarvedData(levelGetter, targetBlockPos);
        if (blockCarvedData != null) {
            callbackInfoReturnable.setReturnValue(blockCarvedData.getInteractionShape());
        }
    }

    // NewStart Inform physics engine that carved blocks are not full solid cubes
    @Inject(
        method = "isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptIsCollisionShapeFullBlock(BlockGetter levelGetter, BlockPos targetBlockPos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (CarvingManager.isCarved(levelGetter, targetBlockPos)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
    // NewEnd
}
