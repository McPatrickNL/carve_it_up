package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeRegistryObject.java

import java.util.function.Supplier;


public class NeoForgeRegistryObject<T> implements RegistryObject<T>
{
    private final Supplier<? extends T> holder;
    
    public NeoForgeRegistryObject(Supplier<? extends T> holder) {
        this.holder = holder;
    }
    
    @Override
    public T get()
    {
        return this.holder.get();
    }
}
