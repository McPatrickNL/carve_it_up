package nl.patrick.carve_it_up;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.registry.NeoForgeBlockRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeCreativeModeTabRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeItemRegistry;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;


// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/CarveItUpNeoForge.java
@Mod(CarveItUpCommon.MOD_ID)
public class CarveItUpNeoForge
{
    public CarveItUpNeoForge(IEventBus modEventBus, ModContainer modContainer)
    {
        // Tell NeoForge to watch our registries
        NeoForgeBlockRegistry.BLOCKS.register(modEventBus);
        NeoForgeItemRegistry.ITEMS.register(modEventBus);
        NeoForgeCreativeModeTabRegistry.TABS.register(modEventBus);
        
        // Initialize the common code setup (which fills tabs, items, etc.)
        
        CarveItUpCommon.init();
        
        modEventBus.addListener(this::addCreativeTabs);
        
    }
    
    private void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Check if the current tab being built matches your custom multiplatform tab
        if (event.getTab() == ModCreativeModeTabs.CARVE_IT_UP_TAB.get()) {
            event.accept(ModItems.EXAMPLE_ITEM.get());
            event.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get());
        }
    }
    
    // todo keep here or keep in CarveItUpNeoForgeClient
//    private static void onClientSetup(FMLClientSetupEvent event) {
//        CarveItUpCommon.LOGGER.info("HELLO FROM NEOFORGE CLIENT SETUP");
//        CarveItUpCommon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
//    }
}