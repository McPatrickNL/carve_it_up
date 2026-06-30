package nl.patrick.carve_it_up.registry;

import net.minecraft.core.component.DataComponentType;

import java.util.function.UnaryOperator;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/registry/BlockRegistry.java
public interface ComponentRegistry
{
    <T> RegistryObject<DataComponentType<T>> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
                                                     );
}
