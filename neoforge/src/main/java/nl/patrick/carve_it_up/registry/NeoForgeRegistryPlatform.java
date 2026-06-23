package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeRegistryPlatform.java

import nl.patrick.carve_it_up.services.IRegistryPlatform;


public class NeoForgeRegistryPlatform
        implements IRegistryPlatform
{
    // Assuming you have created NeoForge-specific wrapper implementations
    private final ItemRegistry items = new NeoForgeItemRegistry();
    private final BlockRegistry blocks = new NeoForgeBlockRegistry();
    private final CreativeModeTabRegistry tabs = new NeoForgeCreativeModeTabRegistry();
    
    @Override
    public ItemRegistry items()
    {
        return this.items;
    }
    
    @Override
    public BlockRegistry blocks()
    {
        return this.blocks;
    }
    
    @Override
    public CreativeModeTabRegistry creativeModeTabs()
    {
        return this.tabs;
    }
}
