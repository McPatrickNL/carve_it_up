package nl.patrick.carve_it_up.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import nl.patrick.carve_it_up.CarveItUpCommon;

import java.util.function.Supplier;


// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricCreativeModeTabRegistry.java

public class FabricCreativeModeTabRegistry implements CreativeModeTabRegistry
{
    @Override
    public RegistryObject<CreativeModeTab> register(String name, Supplier<CreativeModeTab> supplier)
    {
        Identifier id = Identifier.fromNamespaceAndPath(CarveItUpCommon.MOD_ID, name);
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, supplier.get());
        return new FabricRegistryObject<>(tab);
    }
}
