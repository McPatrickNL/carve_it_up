package nl.patrick.carve_it_up.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;


public interface BlockRegistry
{
    RegistryObject<Block> register(
            String name,
            Supplier<Block> supplier);
    
    RegistryObject<BlockItem> registerBlockItem(
            String name,
            Supplier<BlockItem> supplier);
}
