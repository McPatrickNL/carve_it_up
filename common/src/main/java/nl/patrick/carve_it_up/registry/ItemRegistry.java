package nl.patrick.carve_it_up.registry;

import net.minecraft.world.item.Item;

import java.util.function.Function;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/registry/ItemRegistry.java
public interface ItemRegistry
{
    RegistryObject<Item> register(String name, Function<Item.Properties, Item> factory);
}
