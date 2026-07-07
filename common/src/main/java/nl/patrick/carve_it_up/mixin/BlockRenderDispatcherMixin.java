package nl.patrick.carve_it_up.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/BlockRenderDispatcherMixin.java

// When adding new mixins, add them to resources/carve_it_up.mixins.json as well.
@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin
{
    @Inject(
//        method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V",
        method = "renderBatched",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptRender(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, CallbackInfo ci) {
        
        // 1. Fire our high-performance light check method
        if (CarvingManager.isCarved(level, pos)) {
            CarvedData data = CarvingManager.getCarvedData(level, pos);
            
            if (data != null && data.getCustomModel() != null) {
                // 2. Fetch the custom model using the location tracked inside our Chunk structure
                ModelResourceLocation modelLoc = ModelResourceLocation.inventory(data.getCustomModel());
                BakedModel customModel = Minecraft.getInstance().getModelManager().getModel(modelLoc);
                
                // 3. Tessellate our custom carving model geometry directly onto the vertex consumer
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
                        level, customModel, state, pos, poseStack, consumer,
                        checkSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY
                                                                                            );
                
                // 4. Cancel the vanilla render call entirely for this block
                ci.cancel();
            }
        }
    }
}
