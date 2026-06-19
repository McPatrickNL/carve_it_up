package nl.patrick.carve_it_up.block;


import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.registry.ModCommonRegistries.*;

public class ModBlocks
{
    // Creates a new Block with the id "carve_it_up:example_block", combining the namespace and path
//    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    
    // Creates a new BlockItem with the id "carve_it_up:example_block", combining the namespace and path
//    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    
    public static final RegistryObject<Block> EXAMPLE_BLOCK =
            Services.REGISTRY.blocks().register(
                    "example_block",
                    () -> new Block(
                            Block.Properties.of()
                                            .mapColor(MapColor.STONE)));
    
    public static final RegistryObject<BlockItem> EXAMPLE_BLOCK_ITEM =
            Services.REGISTRY.blocks().registerBlockItem(
                    "example_block",
                    () -> new BlockItem(
                            EXAMPLE_BLOCK.get(),
                            new Item.Properties()));
    
    public static void init(){}
}
