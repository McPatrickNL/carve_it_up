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
 * hitbox selection outlines, visual shapes, full-block collision checks, and shade lighting.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

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

    // 5. BLOCK SUPPORT SHAPE
    @Inject(
        method = "getBlockSupportShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptBlockSupportShape(BlockGetter levelGetter, BlockPos targetBlockPos, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
        CarvedData blockCarvedData = CarvingManager.getCarvedData(levelGetter, targetBlockPos);
        if (blockCarvedData != null) {
            callbackInfoReturnable.setReturnValue(blockCarvedData.getCollisionShape());
        }
    }

    // 6. INFORM PHYSICS ENGINE THAT CARVED BLOCKS ARE NOT FULL SOLID CUBES
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

    // NewStart 7. AMBIENT SHADE / LIGHTING BRIGHTNESS FOR CARVED / HOLLOWED BLOCKS (matches stairs, composters, slabs)
    @Inject(
        method = "getShadeBrightness(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptShadeBrightness(BlockGetter levelGetter, BlockPos targetBlockPos, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (CarvingManager.isCarved(levelGetter, targetBlockPos)) {
            callbackInfoReturnable.setReturnValue(1.0F);
        }
    }
    // NewEnd
}
