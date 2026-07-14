package nl.patrick.carve_it_up.item;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/CarvingToolItem.java

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;

import java.util.UUID;

import static nl.patrick.carve_it_up.carving.CarvingManager.debugWipeAllLoadedData;


public class CarvingToolItem extends Item
{
    public CarvingToolItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState originalState = level.getBlockState(pos);
        
        // Prevent carving air, liquids, or unbreakable blocks (like bedrock)
        if (originalState.isAir() || !originalState.getFluidState().isEmpty() || originalState.getDestroySpeed(level, pos) < 0.0F) {
            return InteractionResult.FAIL;
        }
        
        // TODO: Support fragile/non-solid blocks eventually (grass, crops, signs).
        // Currently disabled to prevent physics breaking until we implement custom ticking models.
        
        // 0. CREATIVE + SNEAK + RIGHT CLICK = WIPE ALL DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching() && context.getPlayer().isCreative()) {
            debugWipeAllLoadedData();
            level.sendBlockUpdated(pos, originalState, originalState, 3);
            return InteractionResult.SUCCESS;
        }
        
        // 1. SNEAK + RIGHT CLICK = REMOVE DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            if (CarvingManager.isCarved(level, pos)) {
                CarvingManager.removeCarvedData(level, pos);
                level.sendBlockUpdated(pos, originalState, originalState, 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        // 2. NORMAL RIGHT CLICK = ADD DATA
        if (!CarvingManager.isCarved(level, pos)) {
            // Retrieve vanilla model safely
            net.minecraft.client.renderer.block.dispatch.BlockStateModel vanillaModel = net.minecraft.client.Minecraft.getInstance()
                                                                                                                      .getModelManager()
                                                                                                                      .getBlockStateModelSet()
                                                                                                                      .get(originalState);
            
            UUID owner      = context.getPlayer() != null ? context.getPlayer().getUUID() : UUID.randomUUID();
            int  resolution = 16; // TODO: Hook into your server config value here!
            
            CarvedData data = new CarvedData(
                    originalState,
                    vanillaModel,
                    level,
                    pos,
                    CollisionContext.of(context.getPlayer()),
                    owner,
                    resolution
            );
            
            CarvingManager.setCarvedData(level, pos, data);
            level.sendBlockUpdated(pos, originalState, originalState, 3);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}
