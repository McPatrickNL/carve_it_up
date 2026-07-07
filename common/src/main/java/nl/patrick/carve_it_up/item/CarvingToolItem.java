package nl.patrick.carve_it_up.item;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/CarvingToolItem.java

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;

import static nl.patrick.carve_it_up.carving.CarvingManager.debugWipeAllLoadedData;


public class CarvingToolItem extends Item
{
    public CarvingToolItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level    level = context.getLevel();
        BlockPos pos   = context.getClickedPos();
        BlockState originalState = level.getBlockState(pos);
        if (originalState.isAir()) {
            return InteractionResult.FAIL;
        }
        
        // 0. CREATIVE + SNEAK + RIGHT CLICK = WIPE ALL DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching() && context.getPlayer().isCreative()) {
            debugWipeAllLoadedData();
            // NOTE TO SELF: no block updates are being send here
            return InteractionResult.SUCCESS;
        }
        
        // 1. SNEAK + RIGHT CLICK = REMOVE DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            if (CarvingManager.isCarved(level, pos)) {
                CarvingManager.removeCarvedData(level, pos);
                
                // Force a chunk rerender so the block instantly snaps back visually
                level.sendBlockUpdated(pos, originalState, originalState, 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        // 2. NORMAL RIGHT CLICK = ADD DATA
        if (!CarvingManager.isCarved(level, pos)) {
            // todo pass null for the model for now, this is going to be where the actual magic starts to happen.
            // todo when adding data to for example grass, it breaks the block and it can't be interacted with until relog.
            //  The same thing probably applies to wheat, sugar cane, maybe even signs or chests etc.
            CarvedData data = new CarvedData(
                    originalState,
                    null,
                    level,
                    pos,
                    CollisionContext.of(context.getPlayer())
            );
            
            CarvingManager.setCarvedData(level, pos, data);
            
            // Force the engine to re-bake this block's section mesh immediately
            level.sendBlockUpdated(pos, originalState, originalState, 3);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
//        return InteractionResult.CONSUME;
    }
}
