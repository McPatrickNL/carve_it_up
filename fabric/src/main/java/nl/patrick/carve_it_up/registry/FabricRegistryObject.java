package nl.patrick.carve_it_up.registry;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricRegistryObject.java

public class FabricRegistryObject<T> implements RegistryObject<T>
{
    private final T value;
    
    public FabricRegistryObject(T value) {
        this.value = value;
    }
    
    @Override
    public T get()
    {
        return this.value;
    }
}
