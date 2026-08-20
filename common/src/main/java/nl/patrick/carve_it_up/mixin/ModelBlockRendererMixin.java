package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ModelBlockRendererMixin.java

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.ClientCarvingCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;


@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin
{
    @Inject(
            method = "tesselateBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    public void interceptTesselateBlock(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, CallbackInfo ci)
    {
        // 1. Fire our high-performance light check method
        if (CarvingManager.isCarved(level, pos)) {
            CarvedData data = CarvingManager.getCarvedData(level, pos);
            
            if (data != null) {
                BlockStateModel customModel = ClientCarvingCache.getOrCompute(pos, data.getVersion(), data);
                if (customModel != null && model != customModel) {
                    LOGGER.info("Tesselating carved model for position {}", pos);
                    ((ModelBlockRenderer)(Object)this).tesselateBlock(output, x, y, z, level, pos, blockState, customModel, seed);
                    ci.cancel();
                }
            }
        }
    }
}
