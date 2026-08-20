// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ModelBlockRendererMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;

/**
 * Mixin into ModelBlockRenderer to intercept chunk mesh building and replace
 * default vanilla block models with dynamic greedy-meshed carved block models.
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
        // NewStart Resolve client level safely when levelGetter is a RenderSectionRegion during chunk meshing
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
        // NewEnd
    }
}
