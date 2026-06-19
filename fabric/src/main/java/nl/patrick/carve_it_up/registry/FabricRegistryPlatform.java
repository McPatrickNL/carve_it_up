package nl.patrick.carve_it_up.registry;

import nl.patrick.carve_it_up.services.IRegistryPlatform;

public class FabricRegistryPlatform implements IRegistryPlatform
{
    @Override
    public ItemRegistry items()
    {
        // TODO: Return your Fabric Item registry wrapper implementation
        return null;
    }
    
    @Override
    public BlockRegistry blocks()
    {
        // TODO: Return your Fabric Block registry wrapper implementation
        return null;
    }
    
    @Override
    public CreativeModeTabRegistry creativeModeTabs() // Fixed method name here!
    {
        // TODO: Return your Fabric Tab registry wrapper implementation
        return null;
    }
}