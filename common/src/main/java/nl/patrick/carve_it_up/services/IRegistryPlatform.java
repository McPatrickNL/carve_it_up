package nl.patrick.carve_it_up.services;

import nl.patrick.carve_it_up.registry.BlockRegistry;
import nl.patrick.carve_it_up.registry.CreativeModeTabRegistry;
import nl.patrick.carve_it_up.registry.ItemRegistry;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/services/IRegistryPlatform.java
public interface IRegistryPlatform
{
    ItemRegistry items();
    
    BlockRegistry blocks();
    
    CreativeModeTabRegistry creativeModeTabs();
}
