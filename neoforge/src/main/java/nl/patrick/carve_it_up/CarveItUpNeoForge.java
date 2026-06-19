package nl.patrick.carve_it_up;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;


@Mod(CarveItUpCommon.MOD_ID)
public class CarveItUpNeoForge
{
    public CarveItUpNeoForge(IEventBus modEventBus, ModContainer modContainer)
    {
        CarveItUpCommon.init();
        
        ModNeoForgeRegistries.register(modEventBus);
        
        // Config registration will go here later.
    }
}