// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/MinecraftAttackMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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

        BlockPos hitPos = blockHitResult.getBlockPos();
        var hitBlockState = minecraftInstance.level.getBlockState(hitPos);

        // Prevent carving air, liquids, unbreakable blocks, plants/crops, or chests
        if (hitBlockState.isAir() || !hitBlockState.getFluidState().isEmpty() || hitBlockState.getDestroySpeed(minecraftInstance.level, hitPos) < 0.0F
            || CarvingManager.isCarvingDisallowed(hitBlockState)) {
            return;
        }

        Direction hitDirection = blockHitResult.getDirection();
        CarvingMode selectedMode = CarvingToolClientState.getSelectedMode();
        CarvingPattern selectedPattern = CarvingToolClientState.getSelectedPattern();

        BlockPos targetCarvedPos = hitPos;
        int targetX = 0, targetY = 0, targetZ = 0;

        if (CarvingManager.isCarved(minecraftInstance.level, hitPos)) {
            VoxelCoordinates targetedVoxel = VoxelTargetingUtility.extractTargetedVoxel(blockHitResult);
            if (targetedVoxel == null) {
                return;
            }
            targetX = targetedVoxel.voxelX();
            targetY = targetedVoxel.voxelY();
            targetZ = targetedVoxel.voxelZ();

            // If in ADD mode, offset target voxel by +hitDirection
            if (selectedMode == CarvingMode.ADD) {
                targetX += hitDirection.getStepX();
                targetY += hitDirection.getStepY();
                targetZ += hitDirection.getStepZ();

                // If placement extends beyond 16x16x16 bounds, check if adjacent block is also carved
                if (targetX < 0 || targetX >= 16 || targetY < 0 || targetY >= 16 || targetZ < 0 || targetZ >= 16) {
                    BlockPos adjPos = hitPos.relative(hitDirection);
                    if (CarvingManager.isCarved(minecraftInstance.level, adjPos)) {
                        targetCarvedPos = adjPos;
                        targetX = (targetX % 16 + 16) % 16;
                        targetY = (targetY % 16 + 16) % 16;
                        targetZ = (targetZ % 16 + 16) % 16;
                    } else {
                        callbackInfoReturnable.setReturnValue(false);
                        return;
                    }
                }
            }
        } else {
            // Block is not yet carved: auto-initialize and carve directly on first attack
            VoxelCoordinates targetedVoxel = VoxelTargetingUtility.extractTargetedVoxel(blockHitResult);
            if (targetedVoxel == null) {
                return;
            }
            targetX = targetedVoxel.voxelX();
            targetY = targetedVoxel.voxelY();
            targetZ = targetedVoxel.voxelZ();

            if (selectedMode == CarvingMode.ADD) {
                targetX += hitDirection.getStepX();
                targetY += hitDirection.getStepY();
                targetZ += hitDirection.getStepZ();
                if (targetX < 0 || targetX >= 16 || targetY < 0 || targetY >= 16 || targetZ < 0 || targetZ >= 16) {
                    callbackInfoReturnable.setReturnValue(false);
                    return;
                }
            }
        }

        var selectedMaterial = CarvingToolClientState.getMaterialForTargetBlock(
            minecraftInstance.level,
            targetCarvedPos,
            minecraftInstance.player
        );

        // For LINE mode, step into the block (opposite of clicked face normal)
        Direction carvingDirection = (selectedPattern == CarvingPattern.LINE)
            ? hitDirection.getOpposite()
            : hitDirection;

        RequestCarveActionPayload actionPayload = new RequestCarveActionPayload(
            targetCarvedPos,
            selectedMode,
            selectedPattern,
            targetX, targetY, targetZ,
            selectedMaterial,
            carvingDirection,
            hitDirection,
            selectedPattern.getWidth()
        );
        Services.NETWORK.sendToServer(actionPayload);

        // Cancel vanilla mining entirely
        callbackInfoReturnable.setReturnValue(false);
    }
    // NewEnd

    // NewStart Prevent mining progress ticks and breaking animations while holding the carving tool
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void carveitup$interceptContinueAttack(boolean leftClick, CallbackInfo callbackInfo) {
        if (CarvingToolClientState.isHoldingCarvingTool()) {
            callbackInfo.cancel();
        }
    }
    // NewEnd
}