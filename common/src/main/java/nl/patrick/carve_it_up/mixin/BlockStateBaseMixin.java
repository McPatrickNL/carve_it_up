package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/MixinBlock.java

//@Mixin(Block.class)
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin
{
    // 1. PHYSICAL COLLISION
    @Inject(
//        method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        method = "getCollisionShape*",
        at = @At("HEAD"),
        cancellable = true)
    private void interceptCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        CarvedData data = CarvingManager.getCarvedData(level, pos);
        if (data != null) {
            cir.setReturnValue(data.getCollisionShape());
        }
    }
    
    // 2. SELECTION WIREFRAME / HITBOX OUTLINE
    @Inject(
//        method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        method = "getShape*",
        at = @At("HEAD"),
        cancellable = true)
    private void interceptShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        CarvedData data = CarvingManager.getCarvedData(level, pos);
        if (data != null) {
            cir.setReturnValue(data.getCollisionShape());
        }
    }
    
    // 3. LIGHT / PATHFINDING VISUAL SHAPE
    @Inject(
//        method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        method = "getVisualShape",
        at = @At("HEAD"),
        cancellable = true)
    private void interceptVisualShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        CarvedData data = CarvingManager.getCarvedData(level, pos);
        if (data != null) {
            cir.setReturnValue(data.getVisualShape());
        }
    }
    
    // 4. FACE CULLING / OCCLUSION (Prevents see-through x-ray holes in adjacent blocks)
    @Inject(
//        method = "getFaceOcclusionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        method = "getFaceOcclusionShape",
        at = @At("HEAD"),
        cancellable = true)
    private void interceptFaceOcclusionShape(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<VoxelShape> cir) {
        CarvedData data = CarvingManager.getCarvedData(level, pos);
        if (data != null) {
            // Evaluates the exact 2D footprint slice of your custom 3D VoxelShape on that face
            cir.setReturnValue(data.getCollisionShape().getFaceShape(direction));
        }
    }
}
