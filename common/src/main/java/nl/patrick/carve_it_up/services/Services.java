package nl.patrick.carve_it_up.services;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/services/Services.java

import java.util.ServiceLoader;


public class Services
{
    public static final IRegistryPlatform REGISTRY =
        load(IRegistryPlatform.class);
    
    // NewStart Added platform-agnostic network service, loaded the same way as REGISTRY.
    public static final INetworkPlatform NETWORK =
        load(INetworkPlatform.class);
    // NewEnd
    
    private static <T> T load(Class<T> clazz)
    {
        return ServiceLoader.load(clazz)
                            .findFirst()
                            .orElseThrow();
    }
}