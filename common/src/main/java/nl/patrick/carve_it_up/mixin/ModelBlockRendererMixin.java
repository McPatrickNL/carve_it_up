package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ModelBlockRendererMixin.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
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
            
            if (data != null && data.getNewBlockStateModel() != null) {
                LOGGER.info("CarvedData detected.");
                // 2. Fetch the custom model using the location tracked inside our Chunk structure
//                ModelResourceLocation modelLoc = ModelResourceLocation.inventory(data.getCustomModel());
//                BakedModel customModel = Minecraft.getInstance().getModelManager().getModel(modelLoc);
//
//                // 3. Tessellate our custom carving model geometry directly onto the vertex consumer
//                Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
//                        level, customModel, state, pos, poseStack, consumer,
//                        checkSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY
//                                                                                            );
                
                // 4. Cancel the vanilla render call entirely for this block
                ci.cancel();
            }
        }
    }
}
