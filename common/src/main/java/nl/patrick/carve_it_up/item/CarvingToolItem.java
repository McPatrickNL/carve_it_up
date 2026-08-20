// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/CarvingToolItem.java
package nl.patrick.carve_it_up.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;
import nl.patrick.carve_it_up.services.Services;

import java.util.HashMap;
import java.util.UUID;

import static nl.patrick.carve_it_up.carving.CarvingManager.debugWipeAllLoadedData;

/**
 * Item used to initialize blocks into carvable structures, configure carving modes,
 * and perform carving actions in-world.
 */
public class CarvingToolItem extends Item { // Converted from Allman style brace

    public CarvingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level worldLevel = context.getLevel();
        BlockPos targetBlockPos = context.getClickedPos();
        BlockState originalBlockState = worldLevel.getBlockState(targetBlockPos);

        // Prevent carving air, liquids, or unbreakable blocks (like bedrock)
        if (originalBlockState.isAir() || !originalBlockState.getFluidState().isEmpty() || originalBlockState.getDestroySpeed(worldLevel, targetBlockPos) < 0.0F) {
            return InteractionResult.FAIL;
        }

        // Future Support fragile/non-solid blocks eventually (grass, crops, signs).

        // 0. CREATIVE + SNEAK + RIGHT CLICK = WIPE ALL DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching() && context.getPlayer().isCreative()) {
            debugWipeAllLoadedData();
            worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);
            return InteractionResult.SUCCESS;
        }

        // 1. SNEAK + RIGHT CLICK = REMOVE DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            if (CarvingManager.isCarved(worldLevel, targetBlockPos)) {
                CarvingManager.removeCarvedData(worldLevel, targetBlockPos);
                worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // 2. NORMAL RIGHT CLICK = ADD DATA
        if (!CarvingManager.isCarved(worldLevel, targetBlockPos)) {
            UUID ownerUuid = context.getPlayer() != null ? context.getPlayer().getUUID() : UUID.randomUUID();
            int gridResolution = 16;

            CarvedData freshCarvedData = new CarvedData(
                originalBlockState,
                worldLevel,
                targetBlockPos,
                CollisionContext.of(context.getPlayer()),
                ownerUuid,
                gridResolution
            );

            CarvingManager.setCarvedData(worldLevel, targetBlockPos, freshCarvedData);
            worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);

            // Broadcast the freshly-created carve data to every client tracking this position
            SyncCarvedDataPayload syncPayload = new SyncCarvedDataPayload(
                targetBlockPos,
                freshCarvedData.getOriginalBlockState(),
                freshCarvedData.getOwnerUuid(),
                freshCarvedData.getResolution(),
                freshCarvedData.getVersion(),
                new HashMap<>(freshCarvedData.getVoxelMaterials())
            );
            Services.NETWORK.sendToTrackingClients(worldLevel, targetBlockPos, syncPayload);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}