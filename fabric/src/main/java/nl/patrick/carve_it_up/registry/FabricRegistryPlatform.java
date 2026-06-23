package nl.patrick.carve_it_up.registry;

import nl.patrick.carve_it_up.services.IRegistryPlatform;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/registry/FabricRegistryPlatform.java
public class FabricRegistryPlatform implements IRegistryPlatform
{
    private final ItemRegistry items = new FabricItemRegistry();
    private final BlockRegistry blocks = new FabricBlockRegistry();
    private final CreativeModeTabRegistry tabs = new FabricCreativeModeTabRegistry();
    
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