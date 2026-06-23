package nl.patrick.carve_it_up.registry;

import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/registry/CreativeModeTabRegistry.java
public interface CreativeModeTabRegistry
{
    RegistryObject<CreativeModeTab> register(
            String name,
            Supplier<CreativeModeTab> supplier);
}
