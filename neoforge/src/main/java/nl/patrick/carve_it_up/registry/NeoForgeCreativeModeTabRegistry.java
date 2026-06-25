package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeCreativeModeTabRegistry.java

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.Supplier;


public class NeoForgeCreativeModeTabRegistry implements CreativeModeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CommonMod.MOD_ID);
    
    @Override
    public RegistryObject<CreativeModeTab> register(String name, Supplier<CreativeModeTab> supplier) {
        return new NeoForgeRegistryObject<>(TABS.register(name, supplier));
    }
}
