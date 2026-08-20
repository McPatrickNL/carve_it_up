// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/MinecraftAttackMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into Minecraft client attack methods to intercept left-click attacks,
 * dispatch carving action payloads, and prevent block breaking progress.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {

    // NewStart Intercepts left-click attack to perform carving instead of vanilla mining
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void carveitup$interceptStartAttack(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Minecraft minecraftInstance = Minecraft.getInstance();

        if (!CarvingToolClientState.isHoldingCarvingTool()) {
            return;
        }

        if (minecraftInstance.level == null || !(minecraftInstance.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (!CarvingManager.isCarved(minecraftInstance.level, blockHitResult.getBlockPos())) {
            return;
        }

        VoxelCoordinates targetedVoxel = VoxelTargetingUtility.extractTargetedVoxel(blockHitResult);
        if (targetedVoxel == null) {
            return;
        }

        CarvingMode selectedMode = CarvingToolClientState.getSelectedMode();
        CarvingPattern selectedPattern = CarvingToolClientState.getSelectedPattern();
        var selectedMaterial = CarvingToolClientState.getSelectedMaterialBlock().defaultBlockState();

        int targetX = targetedVoxel.voxelX();
        int targetY = targetedVoxel.voxelY();
        int targetZ = targetedVoxel.voxelZ();

        // If in ADD mode, offset target voxel by +hitDirection to place in the adjacent empty voxel space
        if (selectedMode == CarvingMode.ADD) {
            Direction hitDirection = blockHitResult.getDirection();
            targetX += hitDirection.getStepX();
            targetY += hitDirection.getStepY();
            targetZ += hitDirection.getStepZ();

            // Ignore placement if it would extend beyond the 16x16x16 bounds of this block
            if (targetX < 0 || targetX >= 16 || targetY < 0 || targetY >= 16 || targetZ < 0 || targetZ >= 16) {
                callbackInfoReturnable.setReturnValue(false);
                return;
            }
        }

        // For LINE mode, step into the block (opposite of clicked face normal)
        Direction carvingDirection = (selectedPattern == CarvingPattern.LINE)
            ? blockHitResult.getDirection().getOpposite()
            : blockHitResult.getDirection();

        RequestCarveActionPayload actionPayload = new RequestCarveActionPayload(
            blockHitResult.getBlockPos(),
            selectedMode,
            selectedPattern,
            targetX, targetY, targetZ,
            selectedMaterial,
            carvingDirection,
            blockHitResult.getDirection(),
            CarvingToolClientState.DEFAULT_MULTI_VOXEL_WIDTH
        );
        Services.NETWORK.sendToServer(actionPayload);

        // Cancel vanilla mining entirely
        callbackInfoReturnable.setReturnValue(false);
    }
    // NewEnd

    // NewStart Prevent mining progress ticks and breaking animations while holding the carving tool on carved blocks
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void carveitup$interceptContinueAttack(boolean leftClick, CallbackInfo callbackInfo) {
        Minecraft minecraftInstance = Minecraft.getInstance();
        if (CarvingToolClientState.isHoldingCarvingTool() && minecraftInstance.level != null && minecraftInstance.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            if (CarvingManager.isCarved(minecraftInstance.level, blockHitResult.getBlockPos())) {
                callbackInfo.cancel();
            }
        }
    }
    // NewEnd
}