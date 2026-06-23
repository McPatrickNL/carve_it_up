package nl.patrick.carve_it_up.block;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import nl.patrick.carve_it_up.registry.BlockRegistry;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.CarveItUpCommon.LOGGER;
import static nl.patrick.carve_it_up.registry.ModCommonRegistries.*;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/block/ModBlocks.java
public class ModBlocks
{
    public static final BlockRegistry BLOCKS = Services.REGISTRY.blocks();
    
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
                                                                              properties -> new Block(properties.destroyTime(1.5f))
                                                                             );
    
    public static final RegistryObject<BlockItem> EXAMPLE_BLOCK_ITEM = BLOCKS.registerBlockItem("example_block",
                                                                                                properties -> new BlockItem(EXAMPLE_BLOCK.get(), properties)
                                                                                               );
    
    public static void init(){
        LOGGER.info("Registering Blocks for Carve It Up.");
    }
}
