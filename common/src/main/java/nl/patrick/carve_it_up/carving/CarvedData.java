package nl.patrick.carve_it_up.carving;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvedData.java

import net.minecraft.core.BlockPos;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class CarvedData
{
    private final BlockState       mainBlockState;
    private final Block            mainBlock;
    private final List<Block>      blocks = new ArrayList<>();
    private       VoxelShape       visualShape;
    private       VoxelShape       collisionShape;
    private       VoxelShape       interactionShape;
//    private       ResourceLocation customModel;
    
    // todo should I keep originalCollisionContext as is, or should I always use CollisionContext.empty()?
    public CarvedData(BlockState mainBlockState, Level level, BlockPos blockPos, CollisionContext originalCollisionContext/* , ResourceLocation customModel */)
    {
        this.mainBlockState   = mainBlockState;
        this.mainBlock        = mainBlockState.getBlock();
        this.blocks.add(mainBlock);
        this.visualShape      = mainBlockState.getVisualShape(level, blockPos, originalCollisionContext);
        this.collisionShape   = mainBlockState.getCollisionShape(level, blockPos);
        this.interactionShape = mainBlockState.getInteractionShape(level, blockPos);
//        this.customModel      = customModel;
    }
    
    public BlockState getMainBlockState()      {return mainBlockState;}
    public Block getMainBlock()                {return mainBlock;}
    public List<Block> getBlocks()             {return blocks;}
    public VoxelShape getVisualShape()         {return visualShape;}
    public VoxelShape getCollisionShape()      {return collisionShape;}
    public VoxelShape getInteractionShape()    {return interactionShape;}
//    public ResourceLocation getCustomModel()   { return customModel; }
    
    public void setVisualShape(VoxelShape visualShape)           {this.visualShape = visualShape;}
    public void setCollisionShape(VoxelShape collisionShape)     {this.collisionShape = collisionShape;}
    public void setInteractionShape(VoxelShape interactionShape) {this.interactionShape = interactionShape;}
//    public void setCustomModel(ResourceLocation customModel)     { this.customModel = customModel; }
    
    public boolean hasBlock(Block block)   {return blocks.contains(block);}
    
    public boolean addBlock(Block block)
    {
        if (blocks.contains(block))
        {
            return false;
        }
        blocks.add(block);
        return true;
    }
    
    public boolean removeBlock(Block block)
    {
        if (!blocks.contains(block))
        {
            return false;
        }
        else if (block == this.mainBlock)
        {
            return false;
        }
        // todo Maybe add a safety check to make sure the blocks map is never empty. It should always have the first entry with the mainBlock.
        blocks.remove(block);
        return true;
    }
}
