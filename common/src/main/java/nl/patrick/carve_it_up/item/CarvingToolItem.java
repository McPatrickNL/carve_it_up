package nl.patrick.carve_it_up.item;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/CarvingToolItem.java

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


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
        
        // Prevent mimicking air, bedrocks, or your own mimic block
//        if (originalState.isAir() || originalState.getBlock() instanceof MimicBlock) {
//            return InteractionResult.FAIL;
//        }

//        if (!level.isClientSide) {
//            BlockEntity originalBE = level.getBlockEntity(pos);
//
//            // 1. Swap the block to your mimic block
//            level.setBlock(pos, MyModBlocks.MIMIC_BLOCK.get().defaultBlockState(), 3);
//
//            // 2. Feed the old block state and entity data to your proxy BlockEntity
//            if (level.getBlockEntity(pos) instanceof MimicBlockEntity mimic) {
//                mimic.setMimickedState(originalState, originalBE);
//            }
//
//            // Damage the item
//            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
//            return InteractionResult.SUCCESS;
//        }
        return InteractionResult.CONSUME;
    }
}
