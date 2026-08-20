// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ModelBlockRendererMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;

/**
 * Mixin into ModelBlockRenderer to intercept chunk mesh building, replace default vanilla block models
 * with dynamic greedy-meshed carved block models, and manage neighbor face occlusion.
 */
@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin { // Converted from Allman style brace

    @Inject(
        method = "tesselateBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    public void interceptTesselateBlock(
        BlockQuadOutput outputBuffer,
        float xCoordinate,
        float yCoordinate,
        float zCoordinate,
        BlockAndTintGetter levelGetter,
        BlockPos targetBlockPos,
        BlockState originalBlockState,
        BlockStateModel currentBlockStateModel,
        long randomSeed,
        CallbackInfo callbackInfo
    ) {
        Level activeWorldLevel = levelGetter instanceof Level worldLevelInstance ? worldLevelInstance : Minecraft.getInstance().level;
        if (activeWorldLevel == null) {
            return;
        }

        // 1. Fire our high-performance light check method
        if (CarvingManager.isCarved(activeWorldLevel, targetBlockPos)) {
            CarvedData blockCarvedData = CarvingManager.getCarvedData(activeWorldLevel, targetBlockPos);

            if (blockCarvedData != null) {
                BlockStateModel customBakedModel = ClientCarvingCache.getOrCompute(targetBlockPos, blockCarvedData.getVersion(), blockCarvedData);
                if (customBakedModel != null && currentBlockStateModel != customBakedModel) {
                    LOGGER.info("Tesselating carved model for position {}", targetBlockPos);
                    ((ModelBlockRenderer)(Object)this).tesselateBlock(
                        outputBuffer,
                        xCoordinate,
                        yCoordinate,
                        zCoordinate,
                        levelGetter,
                        targetBlockPos,
                        originalBlockState,
                        customBakedModel,
                        randomSeed
                    );
                    callbackInfo.cancel();
                }
            }
        }
    }

    // NewStart Prevent neighbouring solid blocks from culling their faces against partially-carved blocks (Vanilla/Fabric signature)
    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void interceptShouldRenderFaceFabric(
        BlockAndTintGetter levelGetter,
        BlockState currentBlockState,
        Direction renderDirection,
        BlockPos neighborBlockPos,
        CallbackInfoReturnable<Boolean> callbackInfoReturnable
    ) {
        Level activeWorldLevel = levelGetter instanceof Level worldLevelInstance ? worldLevelInstance : Minecraft.getInstance().level;
        if (activeWorldLevel != null && CarvingManager.isCarved(activeWorldLevel, neighborBlockPos)) {
            CarvedData neighborCarvedData = CarvingManager.getCarvedData(activeWorldLevel, neighborBlockPos);
            if (neighborCarvedData != null) {
                VoxelShape faceOcclusion = neighborCarvedData.getCollisionShape().getFaceShape(renderDirection.getOpposite());
                if (faceOcclusion != Shapes.block()) {
                    callbackInfoReturnable.setReturnValue(true);
                }
            }
        }
    }
    // NewEnd

    // NewStart Prevent neighbouring solid blocks from culling their faces against partially-carved blocks (NeoForge patched signature)
    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void interceptShouldRenderFaceNeoForge(
        BlockAndTintGetter levelGetter,
        BlockPos currentBlockPos,
        BlockState currentBlockState,
        Direction renderDirection,
        BlockPos neighborBlockPos,
        CallbackInfoReturnable<Boolean> callbackInfoReturnable
    ) {
        Level activeWorldLevel = levelGetter instanceof Level worldLevelInstance ? worldLevelInstance : Minecraft.getInstance().level;
        if (activeWorldLevel != null && CarvingManager.isCarved(activeWorldLevel, neighborBlockPos)) {
            CarvedData neighborCarvedData = CarvingManager.getCarvedData(activeWorldLevel, neighborBlockPos);
            if (neighborCarvedData != null) {
                VoxelShape faceOcclusion = neighborCarvedData.getCollisionShape().getFaceShape(renderDirection.getOpposite());
                if (faceOcclusion != Shapes.block()) {
                    callbackInfoReturnable.setReturnValue(true);
                }
            }
        }
    }
    // NewEnd
}
