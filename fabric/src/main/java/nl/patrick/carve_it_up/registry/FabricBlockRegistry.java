package nl.patrick.carve_it_up.registry;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricBlockRegistry.java

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.Function;


public class FabricBlockRegistry implements BlockRegistry
{
    @Override
    public RegistryObject<Block> register(String name, Function<Block.Properties, Block> factory)
    {
        Identifier id = Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, name);
        Block.Properties properties = Block.Properties.of()
                                                      .setId(ResourceKey.create(Registries.BLOCK, id));
        
        Block block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
        return new FabricRegistryObject<>(block);
    }
    
    @Override
    public RegistryObject<BlockItem> registerBlockItem(String name, Function<Item.Properties, BlockItem> factory)
    {
        Identifier id = Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, name);
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id));
        
        BlockItem blockItem = Registry.register(BuiltInRegistries.ITEM, id, factory.apply(properties));
        return new FabricRegistryObject<>(blockItem);
    }
}
