package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeComponentRegistry.java

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.UnaryOperator;


public class NeoForgeComponentRegistry implements ComponentRegistry
{
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CommonMod.MOD_ID);
    
    @Override
    public <T> RegistryObject<DataComponentType<T>> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        
        // NeoForge uses an external RegistryObject wrapper (like NeoForgeRegistryObject)
        return new NeoForgeRegistryObject<>(COMPONENTS.register(name, () ->
                                                                        builderOperator.apply(DataComponentType.builder()).build()
                                                               ));
    }
}
