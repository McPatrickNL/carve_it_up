package nl.patrick.carve_it_up.services;

import nl.patrick.carve_it_up.registry.BlockRegistry;
import nl.patrick.carve_it_up.registry.CreativeModeTabRegistry;
import nl.patrick.carve_it_up.registry.ItemRegistry;


public interface IRegistryPlatform
{
    ItemRegistry items();
    
    BlockRegistry blocks();
    
    CreativeModeTabRegistry creativeModeTabs();
}
