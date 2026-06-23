package nl.patrick.carve_it_up.services;

import java.util.ServiceLoader;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/services/Services.java
public class Services
{
    public static final IRegistryPlatform REGISTRY =
            load(IRegistryPlatform.class);
    
    private static <T> T load(Class<T> clazz)
    {
        return ServiceLoader.load(clazz)
                            .findFirst()
                            .orElseThrow();
    }
}
