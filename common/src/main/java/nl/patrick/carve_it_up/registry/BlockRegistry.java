package nl.patrick.carve_it_up.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/registry/BlockRegistry.java
public interface BlockRegistry
{
    RegistryObject<Block> register(String name, Function<Block.Properties, Block> factory);
    RegistryObject<BlockItem> registerBlockItem(String name, Function<Item.Properties, BlockItem> factory);
}
