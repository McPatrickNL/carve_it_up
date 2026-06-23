package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeBlockRegistry.java

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.patrick.carve_it_up.CarveItUpCommon;

import java.util.function.Function;


public class NeoForgeBlockRegistry implements BlockRegistry
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, CarveItUpCommon.MOD_ID);
    
    @Override
    public RegistryObject<Block> register(String name, Function<Block.Properties, Block> factory)
    {
        return new NeoForgeRegistryObject<>(BLOCKS.register(name, () -> {
            // Automatically pre-configure the ID on the block properties
            Block.Properties properties = Block.Properties.of()
                                                          .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CarveItUpCommon.MOD_ID, name)));
            return factory.apply(properties);
        }));
    }
    
    @Override
    public RegistryObject<BlockItem> registerBlockItem(String name, Function<Item.Properties, BlockItem> factory)
    {
        return new NeoForgeRegistryObject<>(NeoForgeItemRegistry.ITEMS.register(name, () -> {
            // Automatically pre-configure the ID on the block-item properties
            Item.Properties properties = new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CarveItUpCommon.MOD_ID, name)));
            return factory.apply(properties);
        }));
    }
}
