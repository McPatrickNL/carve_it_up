package nl.patrick.carve_it_up.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvingTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/MixinBlockRenderDispatcher.java

// When adding new mixins, add them to resources/carve_it_up.mixins.json as well.
@Mixin(BlockRenderDispatcher.class)
public class MixinBlockRenderDispatcher {
    
    @Inject(method = "renderBatched", at = @At("HEAD"), cancellable = true, remap = false)
    private void interceptRender(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, CallbackInfo ci) {
        
        if (CarvingTracker.isCarved(pos)) {
            // 1. Get the model
            ModelResourceLocation modelLoc = ModelResourceLocation.inventory(CarvingTracker.getCustomModel(pos));
            BakedModel customModel = Minecraft.getInstance().getModelManager().getModel(modelLoc);
            
            // 2. Fetch the correct render type for this block state
            RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
            
            // 3. Render the custom model
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
                    level, customModel, state, pos, poseStack, consumer,
                    checkSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY
                                                                                        );
            
            // 4. Cancel the vanilla render call using the 'ci' parameter
            ci.cancel();
        }
    }
}
