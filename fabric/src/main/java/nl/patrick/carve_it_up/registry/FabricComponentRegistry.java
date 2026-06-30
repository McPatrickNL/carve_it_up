package nl.patrick.carve_it_up.registry;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricComponentRegistry.java

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.UnaryOperator;


public class FabricComponentRegistry implements ComponentRegistry {
    
    @Override
    public <T> RegistryObject<DataComponentType<T>> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        
        Identifier id = Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, name);
        
        DataComponentType<T> componentType = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                id,
                builderOperator.apply(DataComponentType.builder()).build()
                                                              );
        
        return new FabricRegistryObject<>(componentType);
    }
}
