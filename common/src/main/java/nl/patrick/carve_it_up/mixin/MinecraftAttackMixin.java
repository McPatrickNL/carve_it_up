package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/MinecraftAttackMixin.java

// Review Minecraft#startAttack() has been the client-side left-click entry point across several
// recent versions, but confirm the exact method name/signature against the 26.1 mappings (IntelliJ
// "go to declaration" on Minecraft.class) before compiling — this is the one part of this pass I
// couldn't verify against current documentation.

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.carving.CarvingMode;
import nl.patrick.carve_it_up.carving.CarvingPattern;
import nl.patrick.carve_it_up.carving.CarvingToolClientState;
import nl.patrick.carve_it_up.network.RequestCarveActionPayload;
import nl.patrick.carve_it_up.services.Services;
import nl.patrick.carve_it_up.util.VoxelCoordinates;
import nl.patrick.carve_it_up.util.VoxelTargetingUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin
{
    // NewStart Intercepts the client's left-click/attack entry point. If the player is holding
    // the carving tool AND the targeted block is already carved, we cancel vanilla mining entirely
    // and send a carve-action request to the server instead. A right-click on an uncarved block
    // still goes through CarvingToolItem.useOn() as before; left-clicking an uncarved block while
    // holding the tool falls through to vanilla mining unchanged.
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void carveitup$interceptStartAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        
        if (!CarvingToolClientState.isHoldingCarvingTool()) {
            return;
        }
        
        if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        
        if (!CarvingManager.isCarved(mc.level, blockHitResult.getBlockPos())) {
            // Not carved yet — let vanilla mining proceed normally.
            return;
        }
        
        VoxelCoordinates voxel = VoxelTargetingUtility.extractTargetedVoxel(blockHitResult);
        if (voxel == null) {
            return;
        }
        
        CarvingMode mode = CarvingToolClientState.getSelectedMode();
        CarvingPattern pattern = CarvingToolClientState.getSelectedPattern();
        var material = CarvingToolClientState.getSelectedMaterialBlock().defaultBlockState();
        
        RequestCarveActionPayload payload = new RequestCarveActionPayload(
            blockHitResult.getBlockPos(),
            mode,
            pattern,
            voxel.voxelX(), voxel.voxelY(), voxel.voxelZ(),
            material,
            blockHitResult.getDirection(),
            blockHitResult.getDirection(),
            CarvingToolClientState.DEFAULT_MULTI_VOXEL_WIDTH
        );
        Services.NETWORK.sendToServer(payload);
        
        // Cancel vanilla mining — the server will process our carve request instead.
        cir.setReturnValue(false);
    }
    // NewEnd
}