package nl.patrick.carve_it_up.registry;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;


public interface ItemRegistry
{
    RegistryObject<Item> register(
            String name,
            Supplier<Item> supplier);
}
