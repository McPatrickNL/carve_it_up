package nl.patrick.carve_it_up.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.Function;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricItemRegistry.java

public class FabricItemRegistry implements ItemRegistry
{
    @Override
    public RegistryObject<Item> register(String name, Function<Item.Properties, Item> factory)
    {
        Identifier id = Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, name);
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id));
        
        Item item = Registry.register(BuiltInRegistries.ITEM, id, factory.apply(properties));
        return new FabricRegistryObject<>(item);
    }
}
